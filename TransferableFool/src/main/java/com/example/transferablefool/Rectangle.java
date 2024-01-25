package com.example.transferablefool;

public class Rectangle {
    private double x, y, width, height;

    public Rectangle(final double x, final double y, final double width, final double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(final Rectangle rectangle) {
        this.x = rectangle.x;
        this.y = rectangle.y;
        this.width = rectangle.width;
        this.height = rectangle.height;
    }

    public boolean isPointInside(final double x, final double y) {
        return this.x <= x && this.x + this.width >= x && this.y <= y && this.y + this.height >= y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public void setX(final double x) {
        this.x = x;
    }

    public void setY(final double y) {
        this.y = y;
    }

    public void relocate(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    public void move(final double x, final double y) {
        this.x += x;
        this.y += y;
    }

    public void setWidth(final double width) {
        this.width = width;
    }

    public void setHeight(final double height) {
        this.height = height;
    }

    public void resize(final double width, final double height) {
        this.width = width;
        this.height = height;
    }

    public void transform(final double width, final double height) {
        this.width += width;
        this.height += height;
    }
}