package org.telegram.divo.common.compose

import androidx.navigation.NavController

/**
 * Goes back one screen, or calls [onRoot] when already on the graph's start destination.
 *
 * Plain [NavController.popBackStack] also pops the start destination (returning true), which leaves
 * the NavHost empty - a blank white screen - and never hands the back press to the parent.
 */
fun NavController.popBackStackOrElse(onRoot: () -> Unit = {}) {
    if (previousBackStackEntry != null) {
        popBackStack()
    } else {
        onRoot()
    }
}
