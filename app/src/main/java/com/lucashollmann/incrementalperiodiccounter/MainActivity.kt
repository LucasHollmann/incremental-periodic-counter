package com.lucashollmann.incrementalperiodiccounter

import android.Manifest
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lucashollmann.incrementalperiodiccounter.data.Counter
import com.lucashollmann.incrementalperiodiccounter.data.CounterHistoryEntry
import com.lucashollmann.incrementalperiodiccounter.data.NotificationSchedule
import com.lucashollmann.incrementalperiodiccounter.data.NotificationScheduleCodec
import java.util.Calendar
import java.util.Locale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val BorderHighlightRed = Color(0xFFE53935)
private val DarkRed = Color(0xFFC62828)

@Composable
private fun inputFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceTint,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceTint,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
    errorContainerColor = MaterialTheme.colorScheme.surfaceTint,
    focusedBorderColor = BorderHighlightRed,
    errorBorderColor = BorderHighlightRed,
    errorTextColor = MaterialTheme.colorScheme.onSurface,
    errorLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    errorSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: CounterViewModel
    private val postNotificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) requestExactAlarmPermissionIfNeeded()
        }

    private fun requestNotificationPermissionIfNeeded(hasScheduledNotifications: Boolean) {
        if (!hasScheduledNotifications) return

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            postNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        requestExactAlarmPermissionIfNeeded()
    }

    private fun requestExactAlarmPermissionIfNeeded() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
        ) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            viewModel.rescheduleNotifications()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[CounterViewModel::class.java]

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF24242A),
                    onPrimary = Color(0xFFF2F2F3),
                    primaryContainer = Color(0xFF24242A),
                    onPrimaryContainer = Color(0xFFF2F2F3),
                    secondary = Color(0xFFA5A5AD),
                    onSecondary = Color(0xFF151518),
                    secondaryContainer = Color(0xFF24242A),
                    onSecondaryContainer = Color(0xFFF2F2F3),
                    background = Color(0xFF0D0D0F),
                    onBackground = Color(0xFFF2F2F3),
                    surface = Color(0xFF151518),
                    onSurface = Color(0xFFF2F2F3),
                    surfaceVariant = Color(0xFF1C1C21),
                    onSurfaceVariant = Color(0xFFA5A5AD),
                    outline = Color(0xFF303038),
                    outlineVariant = Color(0xFF303038),
                    surfaceTint = Color(0xFF24242A),
                    error = Color(0xFFA5A5AD),
                    onError = Color(0xFFF2F2F3),
                    errorContainer = Color(0xFF24242A),
                    onErrorContainer = Color(0xFFF2F2F3),
                ),
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    CounterScreen(viewModel, ::requestNotificationPermissionIfNeeded)
                }
            }
        }
    }
}

@Composable
private fun CounterScreen(
    viewModel: CounterViewModel,
    requestNotificationPermission: (Boolean) -> Unit,
) {
    val counters by viewModel.counters.collectAsStateWithLifecycle()
    var selectedCounterId by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedHistoryCounterId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showAddCounterDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = selectedCounterId != null || selectedHistoryCounterId != null) {
        selectedCounterId = null
        selectedHistoryCounterId = null
    }

    val historyCounter = counters.find { it.id == selectedHistoryCounterId }
    if (historyCounter != null) {
        CounterHistoryScreen(
            counter = historyCounter,
            history = viewModel.observeCounterHistory(historyCounter.id),
            onBack = { selectedHistoryCounterId = null },
        )
        return
    }

    val selectedCounter = counters.find { it.id == selectedCounterId }
    if (selectedCounter != null) {
        CounterSettingsScreen(
            counter = selectedCounter,
            onBack = { selectedCounterId = null },
            onSaveSettings = {
                    name,
                    normalIncrement,
                    secondaryIncrement,
                    allowNegative,
                    notificationSchedules,
                ->
                viewModel.updateCounterSettings(
                    selectedCounter.id,
                    name,
                    normalIncrement,
                    secondaryIncrement,
                    allowNegative,
                    notificationSchedules,
                )
                requestNotificationPermission(
                    notificationSchedules.any { it.days.isNotEmpty() },
                )
                selectedCounterId = null
            },
            onDelete = {
                viewModel.deleteCounter(selectedCounter.id)
                selectedCounterId = null
            },
        )
        return
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCounterDialog = true },
                containerColor = DarkRed,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = RoundedCornerShape(16.dp),
            ) {
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
                onReset = { counterId, clearHistory ->
                    viewModel.resetCounter(counterId, clearHistory)
                },
                onSetValue = viewModel::setCounterValue,
                onOpenHistory = { selectedHistoryCounterId = it },
                onOpenSettings = { selectedCounterId = it },
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

private fun MainActivity.requestNotificationPermissionIfNeeded(hasScheduledNotifications: Boolean) {
    if (
        hasScheduledNotifications &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
        android.content.pm.PackageManager.PERMISSION_GRANTED
    ) {
        requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
    }
}

@Composable
private fun CounterList(
    counters: List<Counter>,
    onIncrement: (Long, Int) -> Unit,
    onDecrement: (Long, Int) -> Unit,
    onReset: (Long, Boolean) -> Unit,
    onSetValue: (Long, Int) -> Unit,
    onOpenHistory: (Long) -> Unit,
    onOpenSettings: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(counters, key = Counter::id) { counter ->
            CounterCard(
                counter = counter,
                onIncrement = { increment -> onIncrement(counter.id, increment) },
                onDecrement = { increment -> onDecrement(counter.id, increment) },
                onReset = { clearHistory -> onReset(counter.id, clearHistory) },
                onSetValue = { value -> onSetValue(counter.id, value) },
                onOpenHistory = { onOpenHistory(counter.id) },
                onOpenSettings = { onOpenSettings(counter.id) },
            )
        }
    }
}

@Composable
private fun CounterCard(
    counter: Counter,
    onIncrement: (Int) -> Unit,
    onDecrement: (Int) -> Unit,
    onReset: (Boolean) -> Unit,
    onSetValue: (Int) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    var showResetConfirmation by rememberSaveable(counter.id) { mutableStateOf(false) }
    var clearHistoryOnReset by rememberSaveable(counter.id) { mutableStateOf(true) }
    var isEditingValue by rememberSaveable(counter.id) { mutableStateOf(false) }
    var editedValue by rememberSaveable(counter.id) { mutableStateOf(counter.value.toString()) }
    val parsedValue = editedValue.toIntOrNull()
    val canSaveValue = parsedValue != null && (counter.allowNegative || parsedValue >= 0)
    val valueFocusRequester = remember(counter.id) { FocusRequester() }

    LaunchedEffect(isEditingValue) {
        if (isEditingValue) {
            valueFocusRequester.requestFocus()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = counter.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceTint,
                        shape = CircleShape,
                    ) {
                        IconButton(
                            onClick = onOpenHistory,
                            modifier = Modifier.semantics {
                                contentDescription = "Histórico de ${counter.name}"
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceTint,
                        shape = CircleShape,
                    ) {
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier.semantics {
                                contentDescription = "Configurações de ${counter.name}"
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }

            if (isEditingValue) {
                OutlinedTextField(
                    value = editedValue,
                    onValueChange = { input ->
                        editedValue = input.filterIndexed { index, character ->
                            character in '0'..'9' ||
                                (counter.allowNegative && index == 0 && character == '-')
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(valueFocusRequester),
                    colors = inputFieldColors(),
                    label = { Text("Valor") },
                    singleLine = true,
                    isError = editedValue.isNotEmpty() && !canSaveValue,
                    supportingText = {
                        if (editedValue.isNotEmpty() && !canSaveValue) {
                            Text(
                                if (counter.allowNegative) {
                                    "Digite um número inteiro válido."
                                } else {
                                    "Digite um inteiro de 0 a 2.147.483.647."
                                },
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (counter.allowNegative) {
                            KeyboardType.Decimal
                        } else {
                            KeyboardType.Number
                        },
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (canSaveValue) {
                                parsedValue?.let {
                                    onSetValue(it)
                                    isEditingValue = false
                                }
                            }
                        },
                    ),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (canSaveValue) {
                                parsedValue?.let {
                                    onSetValue(it)
                                    isEditingValue = false
                                }
                            }
                        },
                        enabled = canSaveValue,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceTint,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        border = BorderStroke(1.dp, BorderHighlightRed),
                    ) {
                        Text("Salvar valor")
                    }
                    TextButton(
                        onClick = { isEditingValue = false },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceTint,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text("Cancelar")
                    }
                }
            } else {
                Text(
                    text = counter.value.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.clickable {
                        editedValue = counter.value.toString()
                        isEditingValue = true
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val secondaryIncrement = counter.secondaryIncrement
                        ?.takeIf { it != counter.normalIncrement }
                    val stepButtonWeight = if (secondaryIncrement == null) 1f else 0.8f
                    if (secondaryIncrement != null) {
                        StepButton(
                            label = "-$secondaryIncrement",
                            description = "Diminuir incremento secundário: $secondaryIncrement",
                            onClick = { onDecrement(secondaryIncrement) },
                            modifier = Modifier.weight(stepButtonWeight),
                            isSecondary = true,
                        )
                    }
                    StepButton(
                        label = "-${counter.normalIncrement}",
                        description = "Diminuir incremento normal: ${counter.normalIncrement}",
                        onClick = { onDecrement(counter.normalIncrement) },
                        modifier = Modifier.weight(stepButtonWeight),
                    )
                    FilledTonalButton(
                        onClick = {
                            clearHistoryOnReset = true
                            showResetConfirmation = true
                        },
                        modifier = Modifier.weight(if (secondaryIncrement == null) 1.2f else 1.5f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceTint,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text("Zerar", maxLines = 1, style = MaterialTheme.typography.labelLarge)
                    }
                    StepButton(
                        label = "+${counter.normalIncrement}",
                        description = "Aumentar incremento normal: ${counter.normalIncrement}",
                        onClick = { onIncrement(counter.normalIncrement) },
                        modifier = Modifier.weight(stepButtonWeight),
                    )
                    if (secondaryIncrement != null) {
                        StepButton(
                            label = "+$secondaryIncrement",
                            description = "Aumentar incremento secundário: $secondaryIncrement",
                            onClick = { onIncrement(secondaryIncrement) },
                            modifier = Modifier.weight(stepButtonWeight),
                            isSecondary = true,
                        )
                    }
                }
            }
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Deseja zerar o contador ${counter.name}?")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { clearHistoryOnReset = !clearHistoryOnReset },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = clearHistoryOnReset,
                            onCheckedChange = { clearHistoryOnReset = it },
                        )
                        Text("Excluir histórico junto")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReset(clearHistoryOnReset)
                        showResetConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceTint,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text("Zerar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResetConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceTint,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text("Cancelar")
                }
            },
        )
    }
}

@Composable
private fun CounterHistoryScreen(
    counter: Counter,
    history: kotlinx.coroutines.flow.Flow<List<CounterHistoryEntry>>,
    onBack: () -> Unit,
) {
    val entries by remember(counter.id) {
        history
    }.collectAsStateWithLifecycle(initialValue = emptyList())
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("pt", "BR"))
    }
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT)
    }
    val groupedEntries = entries.groupBy { entry ->
        Instant.ofEpochMilli(entry.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(
            onClick = onBack,
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceTint,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text("Voltar")
        }
        Text(
            text = "Histórico: ${counter.name}",
            style = MaterialTheme.typography.headlineMedium,
        )
        if (entries.isEmpty()) {
            Text(
                text = "Nenhuma edição registrada.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                groupedEntries.forEach { (date, dayEntries) ->
                    item(key = "date-$date") {
                        Text(
                            text = date.format(dateFormatter)
                                .replaceFirstChar { it.titlecase(Locale("pt", "BR")) },
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                        )
                    }
                    items(dayEntries, key = CounterHistoryEntry::id) { entry ->
                        val time = Instant.ofEpochMilli(entry.timestamp)
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime()
                            .format(timeFormatter)
                        val formattedStep =
                            if (entry.step > 0) "+${entry.step}" else entry.step.toString()
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            ),
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = time,
                                    modifier = Modifier.padding(end = 16.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Column {
                                    Text("Novo valor: ${entry.newValue}")
                                    Text(
                                        text = "Passo: $formattedStep",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepButton(
    label: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSecondary: Boolean = false,
) {
    val buttonShape = RoundedCornerShape(12.dp)
    val strokeWidth = 1.dp
    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .clip(buttonShape)
            .background(MaterialTheme.colorScheme.surfaceTint)
            .then(
                if (isSecondary) {
                    Modifier.border(
                        BorderStroke(strokeWidth, BorderHighlightRed),
                        buttonShape,
                    )
                } else {
                    Modifier.drawWithContent {
                        drawContent()
                        val stroke = strokeWidth.toPx()
                        val inset = stroke / 2
                        val radius = buttonShape.topStart.toPx(size, this) - inset
                        val path = Path().apply {
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(
                                    inset,
                                    size.height - 2 * radius - inset,
                                    2 * radius + inset,
                                    size.height - inset,
                                ),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = -90f,
                                forceMoveTo = false,
                            )
                            lineTo(size.width - radius, size.height - inset)
                            arcTo(
                                rect = androidx.compose.ui.geometry.Rect(
                                    size.width - 2 * radius - inset,
                                    size.height - 2 * radius - inset,
                                    size.width - inset,
                                    size.height - inset,
                                ),
                                startAngleDegrees = 90f,
                                sweepAngleDegrees = -90f,
                                forceMoveTo = false,
                            )
                        }
                        drawPath(
                            path = path,
                            color = BorderHighlightRed,
                            style = Stroke(width = stroke),
                        )
                    }
                },
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)) {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun CounterSettingsScreen(
    counter: Counter,
    onBack: () -> Unit,
    onSaveSettings: (String, Int, Int?, Boolean, List<NotificationSchedule>) -> Unit,
    onDelete: () -> Unit,
) {
    val context = LocalContext.current
    var name by rememberSaveable(counter.id) { mutableStateOf(counter.name) }
    var normalIncrement by rememberSaveable(counter.id) {
        mutableStateOf(counter.normalIncrement.toString())
    }
    var secondaryIncrement by rememberSaveable(counter.id) {
        mutableStateOf(counter.secondaryIncrement?.toString().orEmpty())
    }
    var allowNegative by rememberSaveable(counter.id) {
        mutableStateOf(counter.allowNegative)
    }
    var notificationSchedulesText by rememberSaveable(counter.id) {
        mutableStateOf(NotificationScheduleCodec.encode(NotificationScheduleCodec.forCounter(counter)))
    }
    var showDeleteConfirmation by rememberSaveable(counter.id) { mutableStateOf(false) }
    val parsedNormalIncrement = normalIncrement.toIntOrNull()?.takeIf { it > 0 }
    val parsedSecondaryIncrement = secondaryIncrement
        .takeIf { it.isNotBlank() }
        ?.toIntOrNull()
        ?.takeIf { it > 0 }
    val notificationSchedules = NotificationScheduleCodec.parse(notificationSchedulesText)
    val weekDays = listOf(
        1 to "Seg",
        2 to "Ter",
        3 to "Qua",
        4 to "Qui",
        5 to "Sex",
        6 to "Sáb",
        7 to "Dom",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextButton(
            onClick = onBack,
            colors = ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceTint,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text("Voltar")
        }
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineMedium,
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            colors = inputFieldColors(),
            label = { Text("Nome do contador") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )
        OutlinedTextField(
            value = normalIncrement,
            onValueChange = { input ->
                normalIncrement = input.filter { it in '0'..'9' }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = inputFieldColors(),
            label = { Text("Incremento normal") },
            singleLine = true,
            isError = parsedNormalIncrement == null,
            supportingText = {
                if (parsedNormalIncrement == null) {
                    Text("Digite um inteiro positivo.")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
        )
        OutlinedTextField(
            value = secondaryIncrement,
            onValueChange = { input ->
                secondaryIncrement = input.filter { it in '0'..'9' }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = inputFieldColors(),
            label = { Text("Incremento secundário") },
            singleLine = true,
            isError = secondaryIncrement.isNotBlank() && parsedSecondaryIncrement == null,
            supportingText = {
                if (secondaryIncrement.isNotBlank() && parsedSecondaryIncrement == null) {
                    Text("Digite um inteiro positivo.")
                }
            },
            placeholder = { Text("Opcional") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(12.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Notificações", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "Cada horário pode ter dias da semana diferentes.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (notificationSchedules.isEmpty()) {
                Text(
                    text = "Nenhum horário configurado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                notificationSchedules.forEachIndexed { scheduleIndex, schedule ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceTint,
                                RoundedCornerShape(12.dp),
                            )
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                schedule.time,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium,
                            )
                            IconButton(
                                onClick = {
                                    notificationSchedulesText = NotificationScheduleCodec.encode(
                                        notificationSchedules.filterIndexed { index, _ ->
                                            index != scheduleIndex
                                        },
                                    )
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remover horário ${schedule.time}",
                                )
                            }
                        }
                        Text(
                            "Dias da semana",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        weekDays.chunked(4).forEach { rowDays ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                rowDays.forEach { (day, label) ->
                                    val isSelected = day in schedule.days
                                    FilledTonalButton(
                                        onClick = {
                                            val updatedSchedule = schedule.copy(
                                                days = (if (isSelected) {
                                                    schedule.days - day
                                                } else {
                                                    schedule.days + day
                                                }).distinct().sorted(),
                                            )
                                            notificationSchedulesText =
                                                NotificationScheduleCodec.encode(
                                                    notificationSchedules.mapIndexed {
                                                            index,
                                                            existing,
                                                        ->
                                                        if (index == scheduleIndex) {
                                                            updatedSchedule
                                                        } else {
                                                            existing
                                                        }
                                                    },
                                                )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = if (isSelected) {
                                            BorderStroke(1.dp, BorderHighlightRed)
                                        } else {
                                            null
                                        },
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.onSurface,
                                        ),
                                        contentPadding = PaddingValues(
                                            horizontal = 2.dp,
                                            vertical = 8.dp,
                                        ),
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Button(
                onClick = {
                    val calendar = Calendar.getInstance()
                    TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            val time = "%02d:%02d".format(Locale.ROOT, hour, minute)
                            if (notificationSchedules.none { it.time == time }) {
                                notificationSchedulesText = NotificationScheduleCodec.encode(
                                    notificationSchedules + NotificationSchedule(
                                        time = time,
                                        days = (1..7).toList(),
                                    ),
                                )
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true,
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceTint,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Adicionar horário", modifier = Modifier.padding(start = 8.dp))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, BorderHighlightRed), RoundedCornerShape(12.dp)),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceTint,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { allowNegative = !allowNegative }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Permitir números negativos",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = if (allowNegative) "Ativado" else "Desativado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = allowNegative,
                        onCheckedChange = { allowNegative = it },
                    )
                }
            }
        }
        Button(
            onClick = {
                val normal = parsedNormalIncrement
                val secondary = parsedSecondaryIncrement
                if (normal != null && (secondaryIncrement.isBlank() || secondary != null)) {
                    onSaveSettings(
                        name,
                        normal,
                        secondary,
                        allowNegative,
                        notificationSchedules,
                    )
                }
            },
            enabled = name.isNotBlank() &&
                parsedNormalIncrement != null &&
                (secondaryIncrement.isBlank() || parsedSecondaryIncrement != null),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .border(BorderStroke(1.dp, BorderHighlightRed), RoundedCornerShape(24.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceTint,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Text("Salvar configurações", style = MaterialTheme.typography.titleMedium)
        }
        OutlinedButton(
            onClick = { showDeleteConfirmation = true },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceTint,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp),
            )
            Text("Excluir contador")
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Excluir contador?") },
            text = { Text("Deseja excluir o contador ${counter.name}? Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceTint,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceTint,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
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
                colors = inputFieldColors(),
                label = { Text("Nome") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceTint,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text("Criar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceTint,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceTint,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text("Cancelar")
            }
        },
    )
}
