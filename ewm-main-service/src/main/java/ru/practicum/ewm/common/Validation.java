package ru.practicum.ewm.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.exception.BadRequestException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Validation {

    public static void checkPage(int from, int size) {
        if (from < 0 || size <= 0) {
            throw new BadRequestException("Pagination parameters are invalid.");
        }
    }
}
