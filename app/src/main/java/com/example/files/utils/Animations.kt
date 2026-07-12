package com.example.files.utils

import android.animation.Animator
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.example.files.Statics.dpToPixels

object Animations {

    @JvmStatic
    fun show(actionBar: View, onAnimationEnd: Runnable) {
        actionBar.animate()
            .translationY(0f)
            .setInterpolator(DecelerateInterpolator(2f))
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {
                    actionBar.visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animator) {
                    onAnimationEnd.run()
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            }).start()
    }

    @JvmStatic
    fun hide(actionBar: View) {
        actionBar.animate()
            .translationY(dpToPixels((actionBar.height + 10).toFloat()).toFloat())
            .setInterpolator(AccelerateInterpolator(2f))
            .setListener(endListener { actionBar.visibility = View.GONE }).start()
    }

    @JvmStatic
    fun hide(actionBar: View, onAnimationEnd: Runnable) {
        actionBar.animate()
            .translationY(dpToPixels((actionBar.height + 10).toFloat()).toFloat())
            .setInterpolator(AccelerateInterpolator(2f))
            .setListener(endListener(onAnimationEnd)).start()
    }

    @JvmStatic
    fun endListener(onAnimationEnd: Runnable): Animator.AnimatorListener {
        return object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                onAnimationEnd.run()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        }
    }
}
