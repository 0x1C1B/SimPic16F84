package org.ai2ra.hso.simpic16f84.ui.util;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Contains syntax highlighting utilities for computing the related parts inside
 * of a text.
 *
 * @see org.fxmisc.richtext.CodeArea
 * @see StyleSpans
 */

public class SyntaxHighlighting {

    public enum Keyword {

        ADDWF, ANDWF, CLRF, CLRW, COMF, DECF, DECFSZ, INCF, INCFSZ, IORWF, MOVF,
        MOVWF, NOP, RLF, RRF, SUBWF, SWAPF, XORWF, BCF, BSF, BTFSC, BTFSS, ADDLW,
        ANDLW, CALL, CLRWDT, GOTO, IORLW, MOVLW, RETFIE, RETLW, RETURN, SLEEP,
        SUBLW, XORLW;

        public static String[] names() {

            return Stream.of(Keyword.values()).map(Keyword::name).map(String::toLowerCase).toArray(String[]::new);
        }
    }

    private static final String KEYWORD_PATTERN;
    private static final String COMMENT_PATTERN;
    private static final Pattern PATTERN;

    static {

        KEYWORD_PATTERN = "\\b(" + String.join("|", Keyword.names()) + ")\\b";
        COMMENT_PATTERN = ";[^\\n]*";

        PATTERN = Pattern.compile(

                String.join("|",

                        String.format("(?<KEYWORD>%s)", KEYWORD_PATTERN),
                        String.format("(?<COMMENT>%s)", COMMENT_PATTERN)
                )
        );
    }

    /**
     * Computes the text styles for syntax highlighting. Only matched pattern will be
     * styled. Other parts of the given text are none styled.
     *
     * @param text The given text it's syntax should be highlighted
     * @return Returns the computed style spans
     */

    public static StyleSpans<Collection<String>> compute(String text) {

        int end = 0;
        Matcher matcher = PATTERN.matcher(text);
        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();

        while(matcher.find()) {

            String style = null != matcher.group("KEYWORD") ? "keyword" :
                    null != matcher.group("COMMENT") ? "comment" : null;

            assert null != style; // Should never happens

            // Set none matching paragraphs in text as none styled

            builder.add(Collections.emptyList(), matcher.start() - end);

            // Style found matcher

            builder.add(Collections.singleton(style), matcher.end() - matcher.start());
            end = matcher.end();
        }

        // Set rest of text as none styled

        builder.add(Collections.emptyList(), text.length() - end);

        return builder.create();
    }
}
