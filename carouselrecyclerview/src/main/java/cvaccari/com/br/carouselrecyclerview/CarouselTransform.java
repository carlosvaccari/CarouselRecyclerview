package cvaccari.com.br.carouselrecyclerview;

import android.view.View;


public class CarouselTransform implements CarouselView.ViewTransformer {
    protected static final float translationXRate = (1) * 0.5f;

    @Override
    public void onAttach(CarouselLayoutManager layoutManager) {
        layoutManager.setDrawOrder(CarouselView.DrawOrder.FirstFront);
    }

    @Override
    public void transform(View view, float position) {
        view.setTranslationX(600*position * translationXRate * (2f / (Math.abs(position) + 2)));
        view.setScaleX(1.5f / (position + 2));
        view.setScaleY(1.8f / (position + 2));
        view.setAlpha(position < 0 ? Math.max(1 + position, 0) : 1);
        view.setVisibility(view.getAlpha() < 0.1 ? View.GONE : View.VISIBLE);
    }
}