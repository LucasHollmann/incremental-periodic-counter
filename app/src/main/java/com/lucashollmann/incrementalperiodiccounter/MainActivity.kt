package com.lucashollmann.incrementalperiodiccounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lucashollmann.incrementalperiodiccounter.data.Counter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider(this)[CounterViewModel::class.java]

        setContent {
            MaterialTheme {
                CounterScreen(viewModel)
            }
        }
    }
}

@Composable
private fun CounterScreen(viewModel: CounterViewModel) {
    val counters by viewModel.counters.collectAsStateWithLifecycle()
    var showAddCounterDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddCounterDialog = true }) {
                Text("Novo contador", modifier = Modifier.padding(horizontal = 16.dp))
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Text(
                text = "Meus contadores",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, bottom = 16.dp),
            )
            CounterList(
                counters = counters,
                onIncrement = viewModel::incrementCounter,
                onDecrement = viewModel::decrementCounter,
                onReset = viewModel::resetCounter,
            )
        }
    }

    if (showAddCounterDialog) {
        AddCounterDialog(
            onDismiss = { showAddCounterDialog = false },
            onCreate = { name ->
                viewModel.addCounter(name)
                showAddCounterDialog = false
            },
        )
    }
}

@Composable
private fun CounterList(
    counters: List<Counter>,
    onIncrement: (Long) -> Unit,
    onDecrement: (Long) -> Unit,
    onReset: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(counters, key = Counter::id) { counter ->
            CounterCard(
                counter = counter,
                onIncrement = { onIncrement(counter.id) },
                onDecrement = { onDecrement(counter.id) },
                onReset = { onReset(counter.id) },
            )
        }
    }
}

@Composable
private fun CounterCard(
    counter: Counter,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onReset: () -> Unit,
) {
    var showResetConfirmation by rememberSaveable(counter.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = counter.name,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = counter.value.toString(),
                style = MaterialTheme.typography.displaySmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onDecrement,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("-1")
                }
                Button(
                    onClick = onIncrement,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("+1")
                }
                TextButton(
                    onClick = { showResetConfirmation = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Zerar")
                }
            }
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Zerar contador?") },
            text = {
                Text("O valor de \"${counter.name}\" (${counter.value}) será substituído por 0.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReset()
                        showResetConfirmation = false
                    },
                ) {
                    Text("Zerar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun AddCounterDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo contador") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name) },
                enabled = name.isNotBlank(),
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
    )
}
