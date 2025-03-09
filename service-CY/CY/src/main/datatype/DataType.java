package main.datatype;

import main.datatype.visitorpattern.Visitor;

/** The type of items which can go over the stream. */
public abstract class DataType {

    public abstract void acceptVisitor(Visitor<?> v);
}
