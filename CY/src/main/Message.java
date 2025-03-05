package main;

import main.datatype.DataType;

public record Message<T extends DataType>(T data) {
}
