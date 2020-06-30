package ru.kostyanoy.entity;

public class Stake implements Cloneable {
    private long tokens;
    private String option;

    public Stake(long tokens, String option) {
        this.tokens = tokens;
        this.option = option;
    }

    public long getTokens() {
        return tokens;
    }

    public String getOption() {
        return option;
    }

    @Override
    public Object clone() {
        return new Stake(this.tokens, this.option);
    }

    @Override
    public String toString() {
        return "Stake{" +
                "tokens=" + tokens +
                ", option='" + option + '\'' +
                '}';
    }
}
