package cvaccari.com.br.carouselview;

public class ItemVO {

    public ItemVO(String text, String color) {
        this.text = text;
        this.color = color;
    }

    String text;

    String color;

    public String getColor() {
        return color;
    }

    public String getText() {
        return text;
    }
}
