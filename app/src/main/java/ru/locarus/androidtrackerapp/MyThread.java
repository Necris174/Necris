package ru.locarus.androidtrackerapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyThread extends Thread{
    private boolean stop = false;

    public MyThread() {
    }

    public MyThread(@Nullable Runnable target) {
        super(target);
    }

    public MyThread(@Nullable ThreadGroup group, @Nullable Runnable target) {
        super(group, target);
    }

    public MyThread(@NonNull String name) {
        super(name);
    }

    public MyThread(@Nullable ThreadGroup group, @NonNull String name) {
        super(group, name);
    }

    public MyThread(@Nullable Runnable target, @NonNull String name) {
        super(target, name);
    }

    public MyThread(@Nullable ThreadGroup group, @Nullable Runnable target, @NonNull String name) {
        super(group, target, name);
    }

    public MyThread(@Nullable ThreadGroup group, @Nullable Runnable target, @NonNull String name, long stackSize) {
        super(group, target, name, stackSize);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        stop = true;
    }

    @Override
    public boolean isInterrupted() {
        return super.isInterrupted()||stop;
    }
}
