package cvaccari.com.br.carouselrecyclerview;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CarouselView extends RecyclerView {
    private static final String TAG = CarouselView.class.getSimpleName();
    private static boolean sIsDebug = false;

    private CarouselLayoutManager mLayoutManager;
    private CarouselView.ViewTransformer mTransformer = CarouselLayoutManager.DEFAULT_TRANSFORMER;
    private boolean mIsInfinite = false;
    private boolean mScrollingAlignToViews, mEnableFling, mClickToScroll;
    private CarouselView.OnScrollListener mOnScrollListener;
    private CarouselView.OnItemClickListener mOnItemClickListener;
    private CarouselView.OnItemSelectedListener mOnItemSelectedListener;
    private int mLastSelectedPosition = Integer.MIN_VALUE;
    private float mLastScrollStartPositionPoint = 0f;
    private boolean mIsScrollTriggeredByUser;

    private RecyclerView.OnScrollListener mInternalOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);

            /**
             * Allowance of scroll offset to scroll to the next item instead of falling back to the current item.
             * 0.3 stands for 30% offset of the item width.
             */
            final float SCROLL_ALIGN_ALLOWANCE = 0.1f;

            switch (newState) {
                case SCROLL_STATE_IDLE: {
                    float newPositionPoint = mLayoutManager.getCurrentPositionPoint();
                    int newPosition = Math.round(newPositionPoint);
                    if (mScrollingAlignToViews && mLayoutManager.getCurrentOffset() != 0) {
                        if (Math.abs(newPositionPoint - newPosition) > SCROLL_ALIGN_ALLOWANCE) {
                            log("> scroll idle %f %f", newPositionPoint - mLastScrollStartPositionPoint, mLayoutManager.getScroller().tweakScrollDx(newPositionPoint - mLastScrollStartPositionPoint));
                            if (mLayoutManager.getScroller().tweakScrollDx(newPositionPoint - mLastScrollStartPositionPoint) > 0) {
                                newPosition = (int) Math.ceil(newPositionPoint);
                            } else {
                                newPosition = (int) Math.floor(newPositionPoint);
                            }
                        }
                        smoothScrollToPosition(newPosition);
                    } else if (mIsScrollTriggeredByUser) {
                        dispatchPositionUpdateMessage(newPosition);
                    }
                    mIsScrollTriggeredByUser = false;
                }
                break;

                case SCROLL_STATE_DRAGGING: {
                    mLastScrollStartPositionPoint = mLayoutManager.getCurrentPositionPoint();
                }
                break;

                case SCROLL_STATE_SETTLING: {

                }
                break;
            }

            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged((CarouselView) recyclerView, newState);
                switch (newState) {
                    case SCROLL_STATE_IDLE: {
                        mOnScrollListener.onScrollEnd((CarouselView) recyclerView);
                    }
                    break;

                    case SCROLL_STATE_DRAGGING: {
                        mOnScrollListener.onScrollBegin((CarouselView) recyclerView);
                    }
                    break;

                    case SCROLL_STATE_SETTLING: {
                        mOnScrollListener.onFling((CarouselView) recyclerView);
                    }
                    break;
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrolled((CarouselView) recyclerView, dx, dy);
                mOnScrollListener.onScrolled((CarouselView) recyclerView,
                        (int) Math.floor(mLayoutManager.getCurrentPositionPoint()),
                        mLayoutManager.translatePosition((int) Math.floor(mLayoutManager.getCurrentPositionPoint())),
                        mLayoutManager.getCurrentOffset()
                );
            }
        }
    };

    public CarouselView(Context context) {
        super(context);
        init();
    }

    public CarouselView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CarouselView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mIsInfinite = false;
        mScrollingAlignToViews = true;
        mEnableFling = true;
        mClickToScroll = true;
        setLayoutManagerInternal(new CarouselLayoutManager());
        mOnScrollListener = null;
        mOnItemClickListener = null;
        super.setOnScrollListener(mInternalOnScrollListener);
    }

    public static boolean isDebug() {
        return sIsDebug;
    }

    /**
     * Set this parameter to control show debug logs from CarouselView or not.
     * @param debug
     */
    public static void setDebug(boolean debug) {
        sIsDebug = debug;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        scrollToPosition(mLayoutManager.getCurrentPosition());
    }

    /**
     * @hide
     *
     * DO NOT USE.
     * @param layout
     */
    @Override
    public final void setLayoutManager(LayoutManager layout) {
        throw new UnsupportedOperationException("CarouselView doesn't support setLayoutManager(LayoutManager)");
    }

    /**
     * @hide
     *
     * DO NOT USE.
     * @param layout
     */
    // TODO to support custom CarouselLayoutManager
    public void setLayoutManager(CarouselLayoutManager layout) {
        if (layout == null) {
            throw new NullPointerException("CarouselLayoutManager cannot be null");
        }
        throw new UnsupportedOperationException("setLayoutManager(CarouselLayoutManager) is not yet supported.");
    }

    private void setLayoutManagerInternal(CarouselLayoutManager layout) {
        if (layout == null) {
            throw new NullPointerException("CarouselLayoutManager cannot be null");
        }
        super.setLayoutManager(layout);
        mLayoutManager = layout;
        mLayoutManager.setInfinite(mIsInfinite);
//        setExtraVisibleChilds(1);
//		setDisplayMode(aDisplayMode);

        mLayoutManager.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Adapter adapter, View view, int position, int adapterPosition) {
                if (mClickToScroll) {
                    smoothScrollToPosition(position);
                }
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(getAdapter(), view, position, adapterPosition);
                }
            }
        });
    }

    /**
     * Returns a CarouselLayoutManager that is in use with this CarouselView.
     * @return
     */
    @Override
    public CarouselLayoutManager getLayoutManager() {
        return mLayoutManager;
    }

    /**
     * Returns the current position. It can be negative or very large if
     * the items are repeating.
     * @return
     * @see CarouselLayoutManager#getCurrentPosition()
     */
    public int getCurrentPosition() {
        return mLayoutManager.getCurrentPosition();
    }

    /**
     * Returns the current adapter position, which is in the range of [0, itemCount).
     * @return
     * @see CarouselLayoutManager#translatePosition(int)
     * @see CarouselLayoutManager#getCurrentPosition()
     */
    public int getCurrentAdapterPosition() {
        return mLayoutManager.translatePosition(mLayoutManager.getCurrentPosition());
    }

    /**
     * Returns the scrolling position in pixel.
     * @return
     * @see CarouselLayoutManager#getCurrentOffset()
     */
    public float getCurrentOffset() {
        return mLayoutManager.getCurrentOffset();
    }

    /**
     * Returns the current position in floating points.
     * @return
     * @see CarouselLayoutManager#getCurrentPositionPoint()
     */
    public float getCurrentPositionPoint() {
        return mLayoutManager.getCurrentPositionPoint();
    }

    /**
     * Returns the last position in floating point when this CarouselView starts to scroll.
     * @return Last position in floating point.
     */
    public float getLastScrollStartPositionPoint() {
        return mLastScrollStartPositionPoint;
    }

    /**
     * Returns whether the given position is valid.
     * @param position
     * @return
     * @see CarouselLayoutManager#isValidPosition(int)
     */
    public boolean isValidPosition(int position) {
        return mLayoutManager.isValidPosition(position);
    }

    /**
     * Smooth scroll to a given position if it is valid. OnItemSelectedListener may be triggered at this point.
     * @param position
     */
    @Override
    public void smoothScrollToPosition(int position) {
        if (!mLayoutManager.isValidPosition(position)) return;
        mIsScrollTriggeredByUser = false;
        super.smoothScrollToPosition(position);
        dispatchPositionUpdateMessage(position);
    }

    /**
     * Scroll to a given position immediately. OnItemSelectedListener may be triggered at this point.
     * @param position
     */
    @Override
    public void scrollToPosition(int position) {
        if (!mLayoutManager.isValidPosition(position)) return;
        super.scrollToPosition(position);
        dispatchPositionUpdateMessage(position);
    }

    /**
     * Trigger OnItemSelectedListener and update selected position.
     * @param position
     */
    private void dispatchPositionUpdateMessage(int position) {
        if (mOnItemSelectedListener != null) {
            if (mLastSelectedPosition != Integer.MIN_VALUE && mLastSelectedPosition != position) {
                mOnItemSelectedListener.onItemDeselected(this, mLastSelectedPosition, mLayoutManager.translatePosition(mLastSelectedPosition), getAdapter());
            }
            mOnItemSelectedListener.onItemSelected(this, position, mLayoutManager.translatePosition(position), getAdapter());
        }
        mLastSelectedPosition = position;
    }

    /**
     * Returns whether enableFling is set.
     * @return
     */
    public boolean isEnableFling() {
        return mEnableFling;
    }

    /**
     * Set enableFling. If enableFling is false, scrolling stops once users' finger releases;
     * otherwise, leave the scrolling behaviour as is.
     *
     * <p />
     *
     * Default value: true
     *
     * @param enableFling
     * @return this
     */
    public CarouselView setEnableFling(boolean enableFling) {
        mEnableFling = enableFling;
        return this;
    }

    /**
     * Returns whether scrollingAlignToViews is set.
     * @return
     */
    public boolean isScrollingAlignToViews() {
        return mScrollingAlignToViews;
    }

    /**
     * Set scrollingAlignToViews. If scrollingAlignToViews is true, the scrolling position will be
     * "corrected" to the nearest integer position after a scrolling ends.
     *
     * <p />
     *
     * Default value: true
     *
     * @param scrollingAlignToViews
     * @return this
     */
    public CarouselView setScrollingAlignToViews(boolean scrollingAlignToViews) {
        mScrollingAlignToViews = scrollingAlignToViews;
        return this;
    }

    /**
     * Returns if clickToScroll is set.
     * @return
     */
    public boolean isClickToScroll() {
        return mClickToScroll;
    }

    /**
     * Set clickToScroll. If clickToScroll is true, this CarouselView will scroll to an item once it is clicked.
     * Note that sometimes it may interfere with other touch events.
     *
     * Default value: true
     *
     * @param clickToScroll
     * @return this
     */
    public CarouselView setClickToScroll(boolean clickToScroll) {
        mClickToScroll = clickToScroll;
        return this;
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        log("CarouselView onMeasure " + getMeasuredWidth() + ", " + getMeasuredHeight());
    }

    /**
     * Set an OnItemClickListener.
     * @param onItemClickListener
     * @return this
     */
    public CarouselView setOnItemClickListener(CarouselView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
        return this;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean result = false;

        final int action = MotionEventCompat.getActionMasked(e);
        // Remark: It seems action = MotionEvent.ACTION_DOWN is never trigged, and only MotionEvent.ACTION_UP and MotionEvent.ACTION_MOVE would be triggered.
        switch (action) {
            case MotionEvent.ACTION_UP:
                if (!mEnableFling) {
                    e.setAction(MotionEvent.ACTION_CANCEL);
                }
                break;

            default:
                mIsScrollTriggeredByUser = true;
                break;
        }
        result = super.onTouchEvent(e);

//		if (mScrollingAlignToViews) {
//			if ((action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL)
//					&& getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
//				smoothScrollToPosition(mLayoutManager.getCurrentPosition());
//			}
//		}
        return result;
    }

    /**
     * Set an OnScrollListener.
     * @param onScrollListener
     * @return this
     */
    public CarouselView setOnScrollListener(CarouselView.OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
        return this;
    }

    /**
     * @hide
     *
     * DO NOT USE.
     * @param listener
     */
    @Deprecated
    @Override
    public void setOnScrollListener(RecyclerView.OnScrollListener listener) {
        throw new UnsupportedOperationException("setOnScrollListener(RecyclerView.OnScrollListener) is not supported, use setOnScrollListener(CarouselView.OnScrollListener) instead.");
    }

    /**
     * Set an OnItemSelectedListener.
     * @param onItemSelectedListener
     * @return this
     */
    public CarouselView setOnItemSelectedListener(CarouselView.OnItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;

        post(new Runnable() {
            @Override
            public void run() {
                int pos = mLayoutManager.getCurrentPosition();
                if (mLayoutManager.isValidPosition(pos)) {
                    dispatchPositionUpdateMessage(pos);
                } else {
                    getAdapter().registerAdapterDataObserver(new AdapterDataObserver() {
                        @Override
                        public void onChanged() {
                            final AdapterDataObserver observer = this;
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    int pos = mLayoutManager.getCurrentPosition();
                                    if (mLayoutManager.isValidPosition(pos)) {
                                        getAdapter().unregisterAdapterDataObserver(observer);
                                        dispatchPositionUpdateMessage(pos);
                                    }
                                }
                            });
                        }
                    });
                }
            }
        });
//		}

        return this;
    }

    /**
     * Display modes/styles of CarouselView. Please refer to the CarouselView demo.
     *
     * <p />
     *
     * Currently, there is only a very limited set of display styles. Please use {@link #Custom}
     * and provide a custom transformation implementation if they do not suit your need.
     */
    @Deprecated
    public enum DisplayMode {
        /**
         * Horizontal list.
         */
        Horizontal,

        /**
         * Wheel carousel.
         */
        Wheel,

        /**
         * Cover-flow carousel.
         */
        CoverFlow,

        /**
         * Time-machine-like carousel.
         */
        TimeMachine,

        /**
         * Time-machine-like carousel in inverse direction.
         *
         * @see #TimeMachine
         */
        InverseTimeMachine,

        /**
         * Simple carousel with customized parameters.
         *
         * @see #setParameter(Parameter, float)
         */
        Parameterized,

        /**
         * Carousel with custom transformation.
         *
         * @see #setTransformer(CarouselView.ViewTransformer)
         */
        Custom;

        /**
         * Returns an array contains all element names of this enum
         * @return
         */
        public static String[] names() {
            CarouselView.DisplayMode[] enums = values();
            String[] names = new String[enums.length];
            for (int i = 0; i < enums.length; i++) {
                names[i] = enums[i].name();
            }
            return names;
        }
    }

    /**
     * Set display style of this CarouselView.
     * @param mode
     * @see CarouselView.DisplayMode
     */
    @Deprecated
    public void setDisplayMode(CarouselView.DisplayMode mode) {
        switch (mode) {

//            case Horizontal:
//                setTransformerInternal(new com.gtomato.android.ui.widget.LinearViewTransformer());
//                break;
//
//            case Wheel:
//                setTransformerInternal(new WheelViewTransformer());
//                break;
//
//            case CoverFlow:
//                setTransformerInternal(new CoverFlowViewTransformer());
//                break;
//
//            case TimeMachine:
//                setTransformerInternal(new TimeMachineViewTransformer());
//                break;
//
//            case InverseTimeMachine:
//                setTransformerInternal(new InverseTimeMachineViewTransformer());
//                break;
//
//            case Parameterized:
//                setTransformerInternal(new ParameterizedViewTransformer());
//                break;

            case Custom:
                setTransformerInternal(mTransformer);
                break;

            default:
                throw new UnsupportedOperationException("Mode " + mode + " is not supported");
        }
    }

    /**
     * Set a view transformer.
     *
     * @param transformer
     *
     * @see LinearViewTransformer
     * @see WheelViewTransformer
     * @see CoverFlowViewTransformer
     * @see TimeMachineViewTransformer
     * @see InverseTimeMachineViewTransformer
     * @see com.gtomato.android.ui.transformer.FlatMerryGoRoundTransformer
     * @see ParameterizableViewTransformer
     */
    public void setTransformer(CarouselView.ViewTransformer transformer) {
        setTransformerInternal(transformer);
    }

    private void setTransformerInternal(CarouselView.ViewTransformer transformer) {
        mTransformer = transformer;
        mLayoutManager.setTransformer(mTransformer);
    }

    /**
     * Returns the transformer currently in use.
     * @return
     */
    public CarouselView.ViewTransformer getTransformer() {
        return mLayoutManager.getTransformer();
    }

    /**
     * Returns whether the items are recurring.
     * @return
     */
    public boolean isInfinite() {
        return mIsInfinite;
    }

    /**
     * Set whether the items are recurring.
     * @param isInfinite
     * @return this
     * @see CarouselLayoutManager#setInfinite(boolean)
     */
    public CarouselView setInfinite(boolean isInfinite) {
        this.mIsInfinite = isInfinite;
        mLayoutManager.setInfinite(mIsInfinite);
        return this;
    }

    /**
     * Returns the number of extra children per side to be preserved and managed by transformations.
     * @return
     */
    public int getExtraVisibleChilds() {
        return mLayoutManager.getExtraVisibleChilds();
    }

    /**
     * Set the number of extra children per side to be preserved and managed by transformations.
     * @param num
     * @return this
     * @see CarouselLayoutManager#setExtraVisibleChilds(CarouselView, int)
     */
    public CarouselView setExtraVisibleChilds(int num) {
        mLayoutManager.setExtraVisibleChilds(this, num);
        return this;
    }

    /**
     * Set how itemviews are positioned.
     * @param gravity
     * @see android.view.Gravity
     */
    public void setGravity(int gravity) {
        mLayoutManager.setGravity(gravity);
    }

    /**
     * Returns how itemviews are positioned.
     * @return
     * @see android.view.Gravity
     */
    public int getGravity() {
        return mLayoutManager.getGravity();
    }

    private static void log(String format, Object... args) {
        if (sIsDebug) {
            if (args.length > 0) {
                Log.d(TAG, String.format(format, args));
            } else {
                Log.d(TAG, format);
            }
        }
    }

    private static void logv(String format, Object... args) {
        if (sIsDebug) {
            if (args.length > 0) {
                Log.v(TAG, String.format(format, args));
            } else {
                Log.v(TAG, format);
            }
        }
    }

    /**
     * Drawing order of item views.
     */
    public enum DrawOrder {
        /**
         * Draws in ascending order. The first view is drawn at bottom-most.
         */
        FirstBack,

        /**
         * Draws in reversed order. The first view is drawn at top-most.
         */
        FirstFront,

        /**
         * The center-most item is drawn at top-most.
         * The two items adjacent to the center-most item is drawn at
         * second top-most, etc.
         */
        CenterFront,

        /**
         * The center-most item is drawn at bottom-most.
         * The two items adjacent to the center-most item is drawn at
         * second bottom-most, etc.
         */
        CenterBack
    }

    /**
     * Definition of a callback to be invoked when a CarouselView has been scrolled.
     */
    public abstract static class OnScrollListener {
        /**
         * Callback method to be invoked when CarouselView is being dragged by outside input such as user touch input.
         * @param carouselView
         */
        public void onScrollBegin(CarouselView carouselView) {}

        /**
         * Callback method to be invoked when CarouselView stops scrolling.
         * @param carouselView
         */
        public void onScrollEnd(CarouselView carouselView) {}

        /**
         * Callback method to be invoked when CarouselView is animating to a final position while not under outside control.
         * @param carouselView
         */
        public void onFling(CarouselView carouselView) {}

        /**
         * Callback method to be invoked when CarouselView's scroll state changes.
         *
         * @param carouselView The CarouselView whose scroll state has changed.
         * @param newState     The updated scroll state. One of {@link #SCROLL_STATE_IDLE},
         *                     {@link #SCROLL_STATE_DRAGGING} or {@link #SCROLL_STATE_SETTLING}.
         */
        public void onScrollStateChanged(CarouselView carouselView, int newState){}

        /**
         * Callback method to be invoked when the CarouselView has been scrolled. This will be
         * called after the scroll has completed.
         *
         * @param carouselView The CarouselView which has been scrolled.
         * @param dx The amount of horizontal scroll.
         * @param dy The amount of vertical scroll.
         */
        public void onScrolled(CarouselView carouselView, int dx, int dy){}

        /**
         * Callback method to be invoked when the CarouselView has been scrolled. This will be
         * called after the scroll has completed.
         * @param carouselView The CarouselView which has been scrolled.
         * @param position {@link #getCurrentPosition() Current position}.
         * @param adapterPosition {@link #getCurrentAdapterPosition() Current adapter position}.
         * @param offset The decimal part of the current position, in the range of [0, 1).
         */
        public void onScrolled(CarouselView carouselView, int position, int adapterPosition, float offset){}
    }

    /**
     * Definition of a callback to be invoked when an item in CarouselView has been clicked.
     */
    public interface OnItemClickListener {
        /**
         * Callback method to be invoked when an item in CarouselView has been clicked.
         * @param adapter
         * @param view
         * @param position {@link #getCurrentPosition() Current position}.
         * @param adapterPosition {@link #getCurrentAdapterPosition() Current adapter position}.
         */
        void onItemClick(Adapter adapter, View view, int position, int adapterPosition);
    }

    /**
     * Definition of a callback to be invoked when an item in CarouselView has been selected.
     */
    public interface OnItemSelectedListener {
        /**
         * Callback method to be invoked when the item is selected or **reselected**.
         * @param carouselView
         * @param position
         * @param adapterPosition
         * @param adapter
         */
        void onItemSelected(CarouselView carouselView, int position, int adapterPosition, Adapter adapter);

        /**
         * Callback method to be invoked when the item changes from selected state to a non-selected state.
         * @param carouselView
         * @param position
         * @param adapterPosition
         * @param adapter
         */
        void onItemDeselected(CarouselView carouselView, int position, int adapterPosition, Adapter adapter);
    }

    /**
     * Interface definition of a view transformer based on position offset.
     */
    public interface ViewTransformer {
        /**
         * Called when attaching the transformer to a CarouselLayoutManager.
         * Allowing to set behaviours of CarouselLayoutManager, e.g. drawOrder.
         *
         * @param layoutManager
         *
         * @see CarouselLayoutManager#setDrawOrder(CarouselView.DrawOrder)
         * @see CarouselLayoutManager#setScroller(CarouselView.Scroller)
         */
        void onAttach(CarouselLayoutManager layoutManager);

        /**
         * Transform a given item view based on position. Usually translate, rotation, alpha, visibility, ... may be altered.
         * @param view Item view
         * @param position For example, 0 for the current center-most item at a stable position;
         *                 4 for 4-th item at the right to the current item;
         *                 -3 for 3-rd item at the left to the current item;
         *                 0.1 for the current item with 10% offset right to its stable position.
         */
        void transform(View view, float position);
    }

    /**
     * Implement this interface if you want to implement a custom scrolling behaviour.
     * For example, inverse scrolling.
     */
    public interface Scroller {
        /**
         * Tweak the value of scroll delta X.
         * @param dx
         * @return new value of scroll delta X
         */
        int tweakScrollDx(int dx);

        /**
         * Tweak the value of scroll delta Y.
         * @param dy
         * @return new value of scroll delta Y
         */
        int tweakScrollDy(int dy);

        /**
         * Reverse the changes made to tweak the value of scroll delta X.
         * @param dx
         * @return new value of scroll delta X
         */
        int inverseTweakScrollDx(int dx);

        /**
         * Reverse the changes made to tweak the value of scroll delta Y.
         * @param dy
         * @return new value of scroll delta Y
         */
        int inverseTweakScrollDy(int dy);

        /**
         * Tweak the value of scroll delta X.
         * @param dx
         * @return new value of scroll delta X
         */
        float tweakScrollDx(float dx);

        /**
         * Tweak the value of scroll delta Y.
         * @param dy
         * @return new value of scroll delta Y
         */
        float tweakScrollDy(float dy);
    }
}