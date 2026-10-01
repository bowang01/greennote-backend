package com.greennote.common.api;

import java.util.List;

public record PageResult<T>(List<T> list, long total) {
}
