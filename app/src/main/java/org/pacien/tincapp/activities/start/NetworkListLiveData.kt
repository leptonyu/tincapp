/*
 * Tinc App, an Android binding and user interface for the tinc mesh VPN daemon
 * Copyright (C) 2017-2018 Pacien TRAN-GIRARD
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.pacien.tincapp.activities.start

import org.pacien.tincapp.activities.common.SelfRefreshingLiveData
import org.pacien.tincapp.context.AppPaths
import java.util.concurrent.TimeUnit

/**
 * @author pacien
 */
class NetworkListLiveData : SelfRefreshingLiveData<List<String>>(1, TimeUnit.SECONDS) {
  private val appPaths = AppPaths

  override fun onRefresh() {
    val networkList = appPaths.confDir().list()?.sorted() ?: emptyList()
    postValue(networkList)
  }
}
