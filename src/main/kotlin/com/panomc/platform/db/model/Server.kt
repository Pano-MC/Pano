package com.panomc.platform.db.model

import com.panomc.platform.util.ServerStatus
import com.panomc.platform.util.ServerType
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet

data class Server(
    val id: Long = -1,
    val name: String,
    val motd: String,
    val host: String,
    val port: Int,
    val playerCount: Long,
    val maxPlayerCount: Long,
    val type: ServerType,
    val version: String,
    val favicon: String,
    val permissionGranted: Boolean = false,
    val status: ServerStatus
) {
    companion object {
        fun from(row: Row) = Server(
            row.getLong(0),
            row.getString(1),
            row.getString(2),
            row.getString(3),
            row.getInteger(4),
            row.getLong(5),
            row.getLong(6),
            ServerType.valueOf(row.getString(7)),
            row.getString(8),
            row.getString(9),
            row.getInteger(10) == 1,
            ServerStatus.valueOf(row.getInteger(11))!!
        )

        fun from(rowSet: RowSet<Row>) = rowSet.map { from(it) }
    }
}