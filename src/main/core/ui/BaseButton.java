package core.ui;

import core.Utils.SimpleColor;

import java.util.function.Consumer;

public abstract class BaseButton<B extends BaseButton<B>> extends BaseElement<B> {
    public boolean isClickable = true, isClicked, oneShot; // TODO перевести на битовые флаги.
    public SimpleColor color;
    public final TextArea name, prompt;
    public Consumer<? super B> clickAction;

    protected BaseButton(Group panel, Style.Text textStyle) {
        super(panel);
        this.name = new TextArea(panel, textStyle);
        this.prompt = new TextArea(panel, textStyle);
    }

    public B setClicked(boolean clicked) {
        this.isClicked = clicked;
        return as();
    }

    public B setPrompt(String prompt) {
        this.prompt.setText(prompt);
        return as();
    }

    public B setOneShot(boolean oneShot) {
        this.oneShot = oneShot;
        return as();
    }

    public B setName(String name) {
        this.name.setText(name);
        return as();
    }

    public B onClick(Consumer<? super B> clickAction) {
        this.clickAction = clickAction;
        return as();
    }

    public B onClick(Runnable clickAction) {
        this.clickAction = b -> clickAction.run();
        return as();
    }

    public B setColor(SimpleColor color) {
        this.color = color;
        return as();
    }

    public B setClickable(boolean isClickable) {
        this.isClickable = isClickable;
        return as();
    }

    public B toggleClickable() {
        this.isClickable = !isClickable;
        return as();
    }
}
