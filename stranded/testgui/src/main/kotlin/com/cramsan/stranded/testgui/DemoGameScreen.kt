package com.cramsan.stranded.testgui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Composable function that generates a screen that displays the state of the client and server, as well as
 * provides controls to make changes to them.
 *
 * @author cramsan
 */
@Suppress("LongMethod", "FunctionNaming")
@Composable
fun DemoGameScreen(
    playerName: String,
    lobbyName: String,
    lobbyId: String,
    gameContent: String,
    playerListContent: String,
    viewModel: DemoGameViewModel,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row {
            Button(
                onClick = { viewModel.onStartServerSelected() }
            ) {
                Text("Start Server")
            }
            Button(
                onClick = { viewModel.onStopServerSelected() }
            ) {
                Text("Stop Server")
            }
        }
        Row {
            Button(
                onClick = { viewModel.onStartClientSelected() }
            ) {
                Text("Start Client")
            }
            Button(
                onClick = { viewModel.onStopClientSelected() }
            ) {
                Text("Stop Client")
            }
        }
        Row {
            TextField(
                modifier = Modifier.weight(1f),
                value = playerName,
                label = { Text(text = "Player name") },
                onValueChange = { viewModel.onPlayerNameUpdated(it) }
            )
            Button(
                onClick = { viewModel.onUpdatePlayerNameSelected() }
            ) {
                Text("Update name")
            }
        }
        Row {
            TextField(
                modifier = Modifier.weight(1f),
                value = lobbyName,
                label = { Text(text = "Lobby name") },
                onValueChange = { viewModel.onLobbyNameUpdated(it) }
            )
            Row {
                Button(
                    onClick = { viewModel.onCreateLobbySelected() }
                ) {
                    Text("Create Lobby")
                }
            }
        }
        Column {
            Row {
                TextField(
                    modifier = Modifier.weight(1f),
                    value = lobbyId,
                    label = { Text(text = "Lobby Id") },
                    onValueChange = { viewModel.onLobbyIdUpdated(it) }
                )
                Button(
                    onClick = { viewModel.onJoinLobbySelected() }
                ) {
                    Text("Join Lobby")
                }
            }
            Row {
                Button(
                    onClick = { viewModel.onLeaveLobbySelected() }
                ) {
                    Text("Leave Lobby")
                }
                Button(
                    onClick = { viewModel.onDeleteLobbySelected() }
                ) {
                    Text("Delete Lobby")
                }
            }
        }
        Button(
            onClick = { viewModel.onListPlayersSelected() }
        ) {
            Text("List Players")
        }
        Row {
            Button(
                onClick = { viewModel.onSetReadySelected() }
            ) {
                Text("Set Ready=True")
            }
            Button(
                onClick = { viewModel.onSetNotReadySelected() }
            ) {
                Text("Set Ready=False")
            }
        }
        Row {
            Button(
                onClick = { viewModel.onStartGameSelected() }
            ) {
                Text("Start Game")
            }
        }
        Row {
            val modifier = Modifier.weight(1f)
            Text(playerListContent, modifier)
            Text(gameContent, modifier)
        }
    }
}
