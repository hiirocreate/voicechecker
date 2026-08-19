package com.vocalrange.analyzer.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/** 依存(Repositoryなど)をコンストラクタ注入するための汎用ViewModelFactory */
class GenericViewModelFactory<T : ViewModel>(private val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
        return creator() as VM
    }
}
