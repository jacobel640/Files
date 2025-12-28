package com.example.files.utils;

import static com.example.files.Statics.dpToPixels;

import android.animation.Animator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class Animations {

    public static void show(View actionBar, Runnable onAnimationEnd) {
        actionBar.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(2))
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        actionBar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        onAnimationEnd.run();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();
    }


    public static void hide(View actionBar) {
        actionBar.animate()
                .translationY(dpToPixels(actionBar.getHeight() + 10))
                .setInterpolator(new AccelerateInterpolator(2))
                .setListener(endListener(() -> actionBar.setVisibility(View.GONE))).start();
    }

    public static void hide(View actionBar, Runnable onAnimationEnd) {
        actionBar.animate()
                .translationY(dpToPixels(actionBar.getHeight() + 10))
                .setInterpolator(new AccelerateInterpolator(2))
                .setListener(endListener(onAnimationEnd)).start();
    }

    public static Animator.AnimatorListener endListener(Runnable onAnimationEnd) {
        return new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onAnimationEnd.run();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };
    }
}
