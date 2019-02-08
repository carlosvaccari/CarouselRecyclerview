package cvaccari.com.br.carouselview;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import cvaccari.com.br.carouselrecyclerview.CarouselView;
import cvaccari.com.br.carouselrecyclerview.CarouselTransform;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<ItemVO> list = new ArrayList<>();
        list.add(new ItemVO("Item 1", "#401e49"));
        list.add(new ItemVO("Item 2", "#895091"));
        list.add(new ItemVO("Item 3", "#023750"));
        list.add(new ItemVO("Item 4", "#14240b"));
        list.add(new ItemVO("Item 5", "#1531b7"));
        list.add(new ItemVO("Item 6", "#2b4251"));
        list.add(new ItemVO("Item 7", "#323339"));
        list.add(new ItemVO("Item 8", "#193446"));
        list.add(new ItemVO("Item 9", "#495542"));


        CarouselView mCarousel = (CarouselView) findViewById(R.id.carousel);
        mCarousel.setTransformer(new CarouselTransform());
        mCarousel.getLayoutManager().resetOptions();
        mCarousel.getLayoutManager().setDrawOrder(CarouselView.DrawOrder.FirstFront);
        mCarousel.setClipChildren(false);
        mCarousel.setExtraVisibleChilds(3);
        mCarousel.setEnableFling(false);
        mCarousel.setAdapter(new CustomAdapter(this, list));
        mCarousel.setClickToScroll(false);
    }
}
