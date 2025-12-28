package com.example.files.listeners;

import com.example.files.actions.DialogBase;

public interface ActionEvent {
    void onActionFinished(DialogBase.Result result);
}
