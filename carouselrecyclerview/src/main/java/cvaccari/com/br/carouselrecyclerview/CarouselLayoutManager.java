package cvaccari.com.br.carouselrecyclerview;

import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

public class CarouselLayoutManager extends RecyclerView.LayoutManager {

    public final static CarouselView.ViewTransformer DEFAULT_TRANSFORMER = new ImmutableTransformer(new LinearViewTransformer());
    public final static CarouselView.Scroller DEFAULT_SCROLLER = new NormalScroller();

    private CarouselView.OnItemClickListener mOnItemClickListener = null;
    private boolean mInfinite = true;
    private CarouselView.DrawOrder mDrawOrder = CarouselView.DrawOrder.FirstBack;
    private int mExtraVisibleChilds = 0;
    private int mGravity = Gravity.CENTER_HORIZONTAL;

    private Queue<Runnable> mPendingTasks = new LinkedList<>();

    @Nullable
    private RecyclerView mRecyclerView = null;
    private int mDecoratedChildWidth, mDecoratedChildHeight;
    private int mLeftOffset = 0, mTopOffset = 0;
    private int mMeasuredWidth = 0, mMeasuredHeight = 0; // mark down measuredWidth, measuredHeight ourselves because getWidth()/getHeight() doesn't update after onMeasure()
    private boolean mHasDatasetUpdated = false;

    private boolean mScrollPositionUpdated = false;

    private int mScrollOffset = 0;

    private CarouselView.ViewTransformer mTransformer = DEFAULT_TRANSFORMER;
    private CarouselView.Scroller mScroller;

    public CarouselLayoutManager() {
        setTransformer(null);
        resetOptions();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        SavedState ss = new SavedState();
        ss.scrollOffset = mScrollOffset;
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        if (!(state instanceof SavedState)) {
            return;
        }

        SavedState ss = (SavedState) state;
        mScrollOffset = ss.scrollOffset;
    }

    /**
     * Provide a custom transformation implementation.
     *
     * @param transformer
     * @return
     */
    public void setTransformer(CarouselView.ViewTransformer transformer) {
        CarouselView.ViewTransformer oldTransformer = this.mTransformer;
        this.mTransformer = transformer != null ? transformer : DEFAULT_TRANSFORMER;
        if (mTransformer != oldTransformer) {
            resetOptions();
            transformer.onAttach(this);
        }
    }

    /**
     * Reset options that may be modified by built-in transformers.
     * <p>
     * Note: Other options, e.g. extraVisibleChilds, that possibly set by
     * custom transformers may have to reset by developers manually.
     */
    public void resetOptions() {
        setScroller(null);
        setDrawOrder(CarouselView.DrawOrder.FirstBack);
    }

    /**
     * Returns the transformer currently in use.
     *
     * @return
     */
    public CarouselView.ViewTransformer getTransformer() {
        return mTransformer;
    }

    /**
     * Returns the number of extra children per side to be preserved and managed by transformations.
     *
     * @return
     */
    public int getExtraVisibleChilds() {
        return mExtraVisibleChilds;
    }

    /**
     * Set the number of extra children per side to be preserved and managed by transformations.
     * <p/>
     * Number of cached children views = (num + 2) * 2 + 1
     *
     * @param carouselView
     * @param num
     * @return
     */
    public CarouselLayoutManager setExtraVisibleChilds(CarouselView carouselView, int num) {
        mExtraVisibleChilds = num;
        carouselView.setItemViewCacheSize((num + 2) * 2 + 1);
        return this;
    }

    /**
     * Returns whether the items are recurring.
     *
     * @return
     */
    public boolean isInfinite() {
        return mInfinite;
    }

    /**
     * Set whether the items are recurring.
     *
     * @param infinite
     * @return
     */
    public CarouselLayoutManager setInfinite(boolean infinite) {
        this.mInfinite = infinite;
        return this;
    }

    /**
     * Return the scroller currently in use.
     *
     * @return
     */
    public CarouselView.Scroller getScroller() {
        return mScroller;
    }

    /**
     * Set the scrolling behaviour as the given implementation.
     *
     * @param scroller
     * @return
     */
    public CarouselLayoutManager setScroller(CarouselView.Scroller scroller) {
        mScroller = scroller != null ? scroller : DEFAULT_SCROLLER;
        return this;
    }

    /**
     * Set how itemviews are positioned.
     *
     * @param gravity
     */
    public void setGravity(int gravity) {
        mGravity = gravity;
        requestLayout();
    }

    /**
     * Returns how itemviews are positioned.
     *
     * @return
     */
    public int getGravity() {
        return mGravity;
    }

    /**
     * Returns the drawing order of the centermost item.
     *
     * @return
     */
    public CarouselView.DrawOrder getDrawOrder() {
        return mDrawOrder;
    }

    /**
     * Set the drawing order of the centermost item.
     *
     * @param drawOrder
     * @return
     */
    public CarouselLayoutManager setDrawOrder(CarouselView.DrawOrder drawOrder) {
        mDrawOrder = drawOrder;
        return this;
    }

    /**
     * Set an OnItemClickListener.
     *
     * @param onItemClickListener
     * @return
     */
    public CarouselLayoutManager setOnItemClickListener(CarouselView.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
        return this;
    }

    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new RecyclerView.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.WRAP_CONTENT);
    }

    /**
     * @return true
     */
    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    /**
     * @return false
     */
    @Override
    public boolean canScrollVertically() {
        return false;
    }

    /**
     * Returns the current X-axis scrolling position in pixels.
     *
     * @return
     */
    public int getScrollX() {
        return mScrollOffset;
    }

    private int getContentLeftX() {
        return (int) (mScrollOffset - getContentWidth() / 2);
    }

    private int getContentRightX() {
        return (int) (mScrollOffset + getContentWidth() / 2);
    }

    private int getLeftmostVisiblePosition() {
        int pos = (int) Math.floor(pixelToPosition(getContentLeftX())) - mExtraVisibleChilds;
        return mInfinite ? pos : Math.max(pos, 0);
    }

    private int getRightmostVisiblePosition() {
        int pos = (int) Math.ceil(pixelToPosition(getContentRightX())) + mExtraVisibleChilds;
        return mInfinite ? pos : Math.min(pos, getItemCount() - 1);
    }

    /**
     * Returns the current position. It can be negative or very large if
     * the items are repeating.
     *
     * @return
     */
    public int getCurrentPosition() {
        return Math.round(pixelToPosition(mScrollOffset));
    }

    /**
     * Returns the current position in floating points.
     *
     * @return
     */
    public float getCurrentPositionPoint() {
        return pixelToPosition(mScrollOffset);
    }

    /**
     * Returns the scrolling position in pixel.
     *
     * @return
     */
    public float getCurrentOffset() {
        float totalOffset = pixelToPosition(mScrollOffset);
        float offset = Math.abs(totalOffset - (float) Math.floor(totalOffset));
        return offset;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (mScroller != null) {
            dx = mScroller.tweakScrollDx(dx);
        }

        if (!mInfinite) {
            if (mScrollOffset + dx < 0) {
                dx = mScrollOffset > 0 ? -mScrollOffset : 0;
            } else {
                int rightmostOffset = mDecoratedChildWidth * (getItemCount() - 1);
                if (mScrollOffset + dx > rightmostOffset) {
                    dx = mScrollOffset < rightmostOffset ? rightmostOffset - mScrollOffset : 0;
                }
            }
        }
        if (dx != 0) {
            mScrollOffset += dx;
            fillChildrenView(recycler, state);
        }

        if (mScroller != null) {
            dx = mScroller.inverseTweakScrollDx(dx);
        }
        return dx;
    }

    /**
     * Returns a position based on a given pixel.
     *
     * @param pixel
     * @return
     */
    protected float pixelToPosition(int pixel) {
        return mDecoratedChildWidth != 0 ? (float) pixel / mDecoratedChildWidth : 0;
    }

    private int getContentWidth() {
        return mMeasuredWidth - getPaddingRight() - getPaddingLeft();
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        mDecoratedChildWidth = 0;
        mDecoratedChildHeight = 0;
        super.onMeasure(recycler, state, widthSpec, heightSpec);
        adjustHostDimension(recycler, state, widthSpec, heightSpec);
    }

    void adjustHostDimension(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);
        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        int width;
        int height;

        mMeasuredWidth = 0;
        mMeasuredHeight = 0;
        measureChildSize(recycler);

        int neededWidth = Math.max(mDecoratedChildWidth, getMinimumWidth());
        int neededHeight = Math.max(mDecoratedChildHeight, getMinimumHeight());

        switch (widthMode) {
            case View.MeasureSpec.EXACTLY: {
                width = widthSize;
            }
            break;

            case View.MeasureSpec.AT_MOST: {
                width = Math.min(neededWidth, widthSize);
            }
            break;

            case View.MeasureSpec.UNSPECIFIED:
            default: {
                width = neededWidth;
            }
            break;
        }

        switch (heightMode) {
            case View.MeasureSpec.EXACTLY: {
                height = heightSize;
            }
            break;

            case View.MeasureSpec.AT_MOST: {
                height = Math.min(neededHeight, heightSize);
            }
            break;

            case View.MeasureSpec.UNSPECIFIED:
            default: {
                height = neededHeight;
            }
            break;
        }
        mMeasuredWidth = width;
        mMeasuredHeight = height;

        setMeasuredDimension(width, height);
    }

    @Override
    public void setMeasuredDimension(int widthSize, int heightSize) {
        super.setMeasuredDimension(widthSize, heightSize);
        mMeasuredWidth = widthSize;
        mMeasuredHeight = heightSize;
    }

    private void measureChildSize(RecyclerView.Recycler recycler) {
        if (getItemCount() > 0 && (getChildCount() == 0 || mDecoratedChildWidth * mDecoratedChildHeight == 0)) {
            // Scrap measure one child
            View scrap = recycler.getViewForPosition(0);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            // Assume every child has the same size.
            mDecoratedChildWidth = getDecoratedMeasuredWidth(scrap);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(scrap);

            detachAndScrapView(scrap, recycler);
        }
    }

    @Override
    public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
        final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();

        final Rect insets = new Rect();
        calculateItemDecorationsForChild(child, insets);
        widthUsed += insets.left + insets.right;
        heightUsed += insets.top + insets.bottom;

        int width = mRecyclerView != null ? mRecyclerView.getWidth() : mMeasuredWidth;
        int height = mRecyclerView != null ? mRecyclerView.getHeight() : mMeasuredHeight;

        final int widthSpec = RecyclerView.LayoutManager.getChildMeasureSpec(width,
                getPaddingLeft() + getPaddingRight() +
                        lp.leftMargin + lp.rightMargin + widthUsed, lp.width,
                false && canScrollHorizontally());
        final int heightSpec = RecyclerView.LayoutManager.getChildMeasureSpec(height,
                getPaddingTop() + getPaddingBottom() +
                        lp.topMargin + lp.bottomMargin + heightUsed, lp.height,
                false && canScrollVertically());
        child.measure(widthSpec, heightSpec);
    }

    private void updateWindowVariables() {
        switch (mGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.LEFT: {
                mLeftOffset = getPaddingLeft();
            }
            break;

            case Gravity.RIGHT: {
                mLeftOffset = mMeasuredWidth - getPaddingRight() - mDecoratedChildWidth;
            }
            break;

            case Gravity.CENTER_HORIZONTAL:
            default: {
                mLeftOffset = (mMeasuredWidth - getPaddingLeft() - getPaddingRight() - mDecoratedChildWidth) / 2 + getPaddingLeft();
            }
            break;
        }

        switch (mGravity & Gravity.VERTICAL_GRAVITY_MASK) {
            case Gravity.TOP:
            default: {
                mTopOffset = getPaddingTop();
            }
            break;

            case Gravity.BOTTOM: {
                mTopOffset = mMeasuredHeight - getPaddingBottom() - mDecoratedChildHeight;
            }
            break;

            case Gravity.CENTER_VERTICAL: {
                mTopOffset = (mMeasuredHeight - getPaddingTop() - getPaddingBottom() - mDecoratedChildHeight) / 2 + getPaddingTop();
            }
            break;
        }
    }

    /**
     * Major layout pass to layout children views.
     *
     * @param recycler
     * @param state
     */
    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        measureChildSize(recycler);
        updateWindowVariables();
        if (state.didStructureChange() || mHasDatasetUpdated || mScrollPositionUpdated) {
            detachAndScrapAttachedViews(recycler);
            mHasDatasetUpdated = false;
            mScrollPositionUpdated = false;
        }
        fillChildrenView(recycler, state);

        final Queue<Runnable> pendingTasks;
        synchronized (this) {
            pendingTasks = mPendingTasks;
            mPendingTasks = new LinkedList<>(); // swap mPendingTasks with a local variable to prevent infinite looping
        }
        post(new Runnable() {
            @Override
            public void run() {
                while (!pendingTasks.isEmpty()) {
                    pendingTasks.poll().run();
                }
            }
        });

    }

    /**
     * Returns an adapter position based on a given absolute position.
     *
     * @param position
     * @return
     */
    public int translatePosition(int position) {
        if (!mInfinite) {
            return position;
        }
        int itemCount = getItemCount();
        position %= itemCount;
        if (position < 0) {
            position += itemCount;
        }
        return position;
    }

    /**
     * Returns whether a given absolution position is valid, which the rules are also based on current value of {@link #isInfinite()}.
     *
     * @param position
     * @return
     */
    public boolean isValidPosition(int position) {
        int itemCount = getItemCount();
        if (itemCount == 0) {
            return false;
        }
        return mInfinite || (0 <= position && position < itemCount);
    }

    /**
     * Re-layout all children views. This is one of the major process during layout, and are frequently called,
     * and thus maximum optimization is needed.
     *
     * @param recycler
     * @param state
     */
    private void fillChildrenView(RecyclerView.Recycler recycler, RecyclerView.State state) {
        MultiSparseArray<View> viewCache = new MultiSparseArray<View>(getChildCount()); // at the end of iteration, all views in viewCache will be recycled

        int leftmostPosition = 0;
        int rightmostPosition = 0;
        int currentPosition = 0;

        // Cache all views by their existing position and detach them
        for (int i = getChildCount() - 1; i >= 0; --i) {
            final View child = getChildAt(i);
            int position = getPosition(child);
            viewCache.put(position, child);

            leftmostPosition = getLeftmostVisiblePosition();
            rightmostPosition = getRightmostVisiblePosition();
            currentPosition = getCurrentPosition();
            detachView(child);
        }

        if (leftmostPosition <= rightmostPosition) {
            // draw all the children views that are in range of [leftmostPosition, rightmostPosition]

            switch (mDrawOrder) {
                case FirstBack:
                case FirstFront: {
                    int posStart, posEnd, posInc;
                    if (mDrawOrder == CarouselView.DrawOrder.FirstFront) {
                        posStart = rightmostPosition;
                        posEnd = leftmostPosition;
                        posInc = -1;
                    } else {
                        posStart = leftmostPosition;
                        posEnd = rightmostPosition;
                        posInc = 1;
                    }

                    int pos = posStart - posInc;
                    do {
                        pos += posInc;
                        drawChild(pos, viewCache, recycler, state);
                    } while (pos != posEnd);
                }
                break;

                case CenterFront: {
                    int left = leftmostPosition, right = rightmostPosition;
                    for (; currentPosition - left > right - currentPosition; ++left) {
                        drawChild(left, viewCache, recycler, state);
                    }
                    for (; currentPosition - left < right - currentPosition; --right) {
                        drawChild(right, viewCache, recycler, state);
                    }
                    // now currentPosition - left == right - currentPosition
                    for (; left < right; ++left, --right) {
                        drawChild(left, viewCache, recycler, state);
                        drawChild(right, viewCache, recycler, state);
                    }
                    drawChild(currentPosition, viewCache, recycler, state);
                }
                break;

                case CenterBack: {
                    drawChild(currentPosition, viewCache, recycler, state);
                    for (int left = currentPosition - 1, right = rightmostPosition;
                         left >= leftmostPosition || right <= rightmostPosition;
                            ) {
                        if (left >= leftmostPosition) {
                            drawChild(left, viewCache, recycler, state);
                            --left;
                        }
                        if (right <= rightmostPosition) {
                            drawChild(right, viewCache, recycler, state);
                            ++right;
                        }
                    }
                }
                break;
            }

        }

        // Recycle views that are not re-attached
        for (int i = viewCache.size() - 1; i >= 0; --i) {
            for (View v : viewCache.valuesAt(i)) {
                recycler.recycleView(v);
            }
        }
    }

    /**
     * Draw a children view at a given position. This is one of the major process during layout, and are frequently called,
     * and thus maximum optimization is needed.
     *
     * @param position
     * @param viewCache
     * @param recycler
     * @param state
     */
    private void drawChild(final int position, MultiSparseArray<View> viewCache, RecyclerView.Recycler recycler, RecyclerView.State state) {
        final int translatedPosition = translatePosition(position);
        View view = viewCache.pop(translatedPosition);
        if (view == null) {
            // retrieve a new/recycled view
            view = recycler.getViewForPosition(translatedPosition);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(null, v, position, translatePosition(position));
                    }
                }
            });
            addView(view);

        } else {
            // re-attach the cached view
            attachView(view);
        }

        measureChildWithMargins(view, 0, 0);

        if (state.isPreLayout()) {
            return;
        }

        layoutDecorated(view, mLeftOffset, mTopOffset,
                mLeftOffset + mDecoratedChildWidth,
                mTopOffset + mDecoratedChildHeight);

        mTransformer.transform(view, -(pixelToPosition(mScrollOffset) - position));
    }

    @Override
    public void scrollToPosition(final int position) {
        if (mDecoratedChildWidth == 0 && getItemCount() > 0) {
            mPendingTasks.add(new Runnable() {
                @Override
                public void run() {
                    scrollToPosition(position);
                }
            });
            return;
        }
        int newOffset = position * mDecoratedChildWidth;
        if (Math.abs(newOffset - mScrollOffset) > mDecoratedChildWidth * 1.5) {
            mScrollPositionUpdated = true;
        }
        mScrollOffset = newOffset;
        if (mRecyclerView != null && !mRecyclerView.isInLayout()) {
            requestLayout();
        }
    }

    @Override
    public void smoothScrollToPosition(final RecyclerView recyclerView, final RecyclerView.State state,
                                       int position) {
        int minScrollOffset = Integer.MAX_VALUE;
        final int nChilds = getItemCount();

        if (mDecoratedChildWidth == 0 && nChilds > 0) {
            final int finalPosition = position;
            mPendingTasks.add(new Runnable() {
                @Override
                public void run() {
                    smoothScrollToPosition(recyclerView, state, finalPosition);
                }
            });
            return;
        }
        if (mDecoratedChildWidth * nChilds == 0) {
            return;
        }

        if (!isInfinite()) {
            position = Math.max(0, Math.min(nChilds - 1, position));
        } else {
            position %= nChilds;
        }

        for (int round = -1; round <= 1; ++round) {
            if (isInfinite() || round == 0) {
                int offset = ((position + round * nChilds) * mDecoratedChildWidth) - (mScrollOffset % (mDecoratedChildWidth * nChilds));
                if (Math.abs(offset) < Math.abs(minScrollOffset)) {
                    minScrollOffset = offset;
                }
            }
        }
        recyclerView.smoothScrollBy(minScrollOffset, 0);
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        super.onAdapterChanged(oldAdapter, newAdapter);
        removeAllViews();
    }

    @Override
    public void onItemsChanged(@NonNull RecyclerView recyclerView) {
        super.onItemsChanged(recyclerView);
        mHasDatasetUpdated = true;
    }

    @Override
    public void onItemsUpdated(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsUpdated(recyclerView, positionStart, itemCount);
        mHasDatasetUpdated = true;
        for (int i = 0; i < itemCount; ++i) {
            View view = findViewByPosition(positionStart + i);
            if (view != null) {
                view.forceLayout();
            }
        }
    }

    @Override
    public void onItemsAdded(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsAdded(recyclerView, positionStart, itemCount);
        mHasDatasetUpdated = true;
    }

    @Override
    public void onItemsRemoved(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
        super.onItemsRemoved(recyclerView, positionStart, itemCount);
        mHasDatasetUpdated = true;
    }

    @Override
    public void onItemsMoved(@NonNull RecyclerView recyclerView, int from, int to, int itemCount) {
        super.onItemsMoved(recyclerView, from, to, itemCount);
        mHasDatasetUpdated = true;
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        super.onAttachedToWindow(view);
        mRecyclerView = view;
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        mRecyclerView = null;
    }

    /**
     * Retrieve the underlying CarouselView.
     *
     * @return
     */
    @Nullable
    protected CarouselView getCarouselView() {
        return (CarouselView) mRecyclerView;
    }

    /**
     * Add a given runnable action to the underlying CarouselView.
     *
     * @param action
     * @return whether the given action is successfully queued
     * @see CarouselView#post(Runnable)
     */
    protected boolean post(Runnable action) {
        if (mRecyclerView == null) {
            return false;
        }

        mRecyclerView.post(action);
        return true;
    }

    static class SavedState implements Parcelable {

        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        int scrollOffset;

        SavedState() {

        }

        private SavedState(Parcel in) {
            scrollOffset = in.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(scrollOffset);
        }
    }
}