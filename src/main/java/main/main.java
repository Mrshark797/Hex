package main;

import Model.TransformIntoHEX;
import UI.Frame;

public class main {
    public static void main(String[] args){

        TransformIntoHEX transformIntoHEX = new TransformIntoHEX();
        Frame jFrame = new Frame(transformIntoHEX);
        jFrame.createFrame();
    }
}
