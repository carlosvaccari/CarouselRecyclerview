# CarouselRecyclerview

Step 1. Add the JitPack repository to your build file. Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
Step 2. Add the dependency
```
	dependencies {
	        implementation 'com.github.carlosvaccari:CarouselRecyclerview:Tag'
	}
```
  
  In your layout_file.xml
```  
    <cvaccari.com.br.carouselrecyclerview.CarouselView
          android:id="@+id/carousel"
          android:layout_width="match_parent"
          android:layout_height="350dp"/>
 ```
 
  In yout ClassFile.java
  ```
        CarouselView mCarousel = (CarouselView) findViewById(R.id.carousel);
        mCarousel.setTransformer(new CarouselTransform());
        mCarousel.setExtraVisibleChilds(3);
        mCarousel.setEnableFling(false);
        mCarousel.setAdapter(new CustomAdapter(this, list));
        mCarousel.setClickToScroll(false);
```
