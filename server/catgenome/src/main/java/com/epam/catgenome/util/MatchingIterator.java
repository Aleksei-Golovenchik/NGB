/*
 * MIT License
 *
 * Copyright (c) 2021 EPAM Systems
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.epam.catgenome.util;

import lombok.Value;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.NoSuchElementException;

public class MatchingIterator implements Iterator<MatchingIterator.Match> {

    private final List<PeekableMatcher> matchers;

    public MatchingIterator(final Matcher... matchers) {
        this.matchers = Arrays.stream(matchers)
                .map(PeekableMatcher::new)
                .collect(Collectors.toList());
        enumerateMatchers(this.matchers);
    }

    @Override
    public boolean hasNext() {
        return matchers.stream().anyMatch(PeekableMatcher::hasNext);
    }

    @Override
    public Match next() {
        final PeekableMatcher peekableMatcher = matchers.stream()
                .filter(PeekableMatcher::hasNext)
                .min(Comparator.comparing(PeekableMatcher::start))
                .orElseThrow(() -> new NoSuchElementException("\"next()\" invoked without invoking \"hasNext()\"!"));
        peekableMatcher.find();
        return new Match(peekableMatcher.matcherIndex,
                peekableMatcher.start(),
                peekableMatcher.end()
        );
    }

    private void enumerateMatchers(final List<PeekableMatcher> matchers) {
        for (int i = 0; i < matchers.size(); i++) {
            matchers.get(i).matcherIndex = i;
        }
    }

    private static final class PeekableMatcher {

        private final Matcher matcher;
        private boolean nextValueAvailable;
        private boolean hasNextInvoked;
        private int currentPosition;
        private int matcherIndex;



        private PeekableMatcher(final Matcher matcher) {
            this.matcher = matcher;
        }

        private boolean hasNext() {
            if (!hasNextInvoked) {
                nextValueAvailable = matcher.find(currentPosition);
                hasNextInvoked = true;
                if (nextValueAvailable) {
                    currentPosition = matcher.start() + 1;
                }
            }
            return nextValueAvailable;
        }

        private boolean find() {
            if (!hasNextInvoked) {
                throw new IllegalStateException("\"find()\" invoked without invoking \"hasNext()\"!");
            }
            hasNextInvoked = false;
            return nextValueAvailable;
        }

        private int start() {
            return matcher.start();
        }

        private int end() {
            return matcher.end();
        }
    }

    @Value
    public static class Match {
        Integer matcherIndex;
        Integer matchingStartResult;
        Integer matchingEndResult;
    }
}
