package com.leap12.common;

public interface Coercer<SRC, DST> {
	DST coerce( SRC val );
}
