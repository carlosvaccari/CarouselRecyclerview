package cvaccari.com.br.carouselrecyclerview;

import android.view.View;


public class ImmutableTransformer implements CarouselView.ViewTransformer {

    private CarouselView.ViewTransformer mTransformer;

    public ImmutableTransformer(CarouselView.ViewTransformer transformer) {
        mTransformer = transformer;
    }

    @Override
    public void onAttach(CarouselLayoutManager layoutManager) {

    }

    @Override
    public final void transform(View view, float position) {
        mTransformer.transform(view, position);
    }
}