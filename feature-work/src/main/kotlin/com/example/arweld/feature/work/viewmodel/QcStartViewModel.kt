package com.example.arweld.feature.work.viewmodel

import androidx.core.net.toFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arweld.core.domain.model.WorkItem
import com.example.arweld.core.domain.work.WorkRepository
import com.example.arweld.core.domain.work.usecase.FailQcUseCase
import com.example.arweld.core.domain.work.usecase.FailQcInput
import com.example.arweld.core.domain.work.usecase.PassQcInput
import com.example.arweld.core.domain.work.usecase.PassQcUseCase
import com.example.arweld.core.domain.work.usecase.StartQcInspectionUseCase
import com.example.arweld.core.domain.work.usecase.QcDecisionResult
import com.example.arweld.core.domain.evidence.EvidenceKind
import com.example.arweld.core.domain.event.EventType
import com.example.arweld.core.domain.event.EventRepository
import com.example.arweld.core.domain.evidence.EvidenceRepository
import com.example.arweld.core.domain.policy.QcEvidencePolicy
import com.example.arweld.core.domain.work.model.QcChecklistResult
import com.example.arweld.feature.work.camera.PhotoCaptureService
import com.example.arweld.feature.arview.arcore.ArScreenshotRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Starts a QC inspection and surfaces the minimal WorkItem details required for the entry screen.
 */
@HiltViewModel
class QcStartViewModel @Inject constructor(
    private val startQcInspectionUseCase: StartQcInspectionUseCase,
    private val workRepository: WorkRepository,
    private val eventRepository: EventRepository,
    private val evidenceRepository: EvidenceRepository,
    private val qcEvidencePolicy: QcEvidencePolicy,
    private val passQcUseCase: PassQcUseCase,
    private val failQcUseCase: FailQcUseCase,
    private val photoCaptureService: PhotoCaptureService,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QcStartUiState(isLoading = true))
    val uiState: StateFlow<QcStartUiState> = _uiState.asStateFlow()

    private var initializedForWorkItemId: String? = null

    fun onMissingWorkItem() {
        initializedForWorkItemId = null
        _uiState.value = QcStartUiState(
            isLoading = false,
            actionInProgress = false,
            errorMessage = "Work item not found",
        )
    }

    fun start(workItemId: String) {
        if (initializedForWorkItemId == workItemId) {
            refreshPolicy(workItemId)
            return
        }
        initializedForWorkItemId = workItemId

        viewModelScope.launch {
            _uiState.value = QcStartUiState(isLoading = true)
            runCatching {
                startQcInspectionUseCase(workItemId)
                val workItem = workRepository.getWorkItemById(workItemId)
                    ?: error("Work item not found")
                val policy = loadPolicy(workItem.id)

                LoadedPayload(
                    workItem = workItem,
                    policy = policy,
                )
            }.onSuccess { payload ->
                _uiState.value = QcStartUiState(
                    isLoading = false,
                    actionInProgress = false,
                    workItemId = payload.workItem.id,
                    code = payload.workItem.code,
                    zone = payload.workItem.zone,
                    canCompleteQc = payload.policy.canComplete,
                    missingEvidence = payload.policy.missingEvidence,
                    latestQcStartedEventId = payload.policy.latestQcStartedEventId,
                    evidenceCount = payload.policy.evidenceCount,
                    evidenceCounts = payload.policy.evidenceCounts,
                    errorMessage = null,
                )
            }.onFailure { throwable ->
                initializedForWorkItemId = null
                _uiState.value = QcStartUiState(
                    isLoading = false,
                    actionInProgress = false,
                    workItemId = workItemId,
                    errorMessage = throwable.message ?: "Failed to start QC",
                )
            }
        }
    }

    fun onPassQc() {
        performQcOutcome { workItemId ->
            passQcUseCase(
                PassQcInput(
                    workItemId = workItemId,
                    checklist = QcChecklistResult(emptyList()),
                    comment = null,
                ),
            )
        }
    }

    fun onFailQc() {
        performQcOutcome { workItemId ->
            failQcUseCase(
                FailQcInput(
                    workItemId = workItemId,
                    checklist = QcChecklistResult(emptyList()),
                    reasons = emptyList(),
                    priority = 0,
                    comment = null,
                ),
            )
        }
    }

    fun refreshPolicy(workItemId: String? = null) {
        val idToCheck = workItemId ?: _uiState.value.workItemId ?: return
        viewModelScope.launch {
            runCatching { loadPolicy(idToCheck) }
                .onSuccess { policy ->
                    _uiState.value = _uiState.value.copy(
                        canCompleteQc = policy.canComplete,
                        missingEvidence = policy.missingEvidence,
                        latestQcStartedEventId = policy.latestQcStartedEventId,
                        evidenceCount = policy.evidenceCount,
                        evidenceCounts = policy.evidenceCounts,
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        canCompleteQc = false,
                        missingEvidence = REQUIRED_KINDS,
                        actionErrorMessage = throwable.message
                            ?: "Не удалось проверить требования доказательств",
                    )
                }
        }
    }

    fun capturePhoto(workItemId: String) {
        val qcEventId = _uiState.value.latestQcStartedEventId
            ?: run {
                _uiState.value = _uiState.value.copy(
                    actionErrorMessage = "Начните с QC_START для привязки фото",
                )
                return
            }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, actionErrorMessage = null)
            runCatching {
                val captureResult = photoCaptureService.capturePhoto()
                evidenceRepository.savePhoto(
                    workItemId = workItemId,
                    eventId = qcEventId,
                    file = captureResult.uri.toFile(),
                )
                loadPolicy(workItemId)
            }
                .onSuccess { policy ->
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        canCompleteQc = policy.canComplete,
                        missingEvidence = policy.missingEvidence,
                        latestQcStartedEventId = policy.latestQcStartedEventId,
                        evidenceCount = policy.evidenceCount,
                        evidenceCounts = policy.evidenceCounts,
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        actionErrorMessage = throwable.message ?: "Не удалось сохранить фото",
                    )
                }
        }
    }

    fun captureArScreenshot(workItemId: String) {
        val qcEventId = _uiState.value.latestQcStartedEventId
            ?: run {
                _uiState.value = _uiState.value.copy(
                    actionErrorMessage = "Начните с QC_START для привязки скриншота",
                )
                return
            }

        val screenshotService = ArScreenshotRegistry.current()
        if (screenshotService == null) {
            _uiState.value = _uiState.value.copy(
                actionErrorMessage = "AR просмотр не активен для скриншота",
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(actionInProgress = true, actionErrorMessage = null)
            runCatching {
                val uri = screenshotService.captureArScreenshotToFile(workItemId)
                val meta = screenshotService.currentScreenshotMeta()
                evidenceRepository.saveArScreenshot(
                    workItemId = workItemId,
                    eventId = qcEventId,
                    uri = uri,
                    meta = meta,
                )
                loadPolicy(workItemId)
            }
                .onSuccess { policy ->
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        canCompleteQc = policy.canComplete,
                        missingEvidence = policy.missingEvidence,
                        latestQcStartedEventId = policy.latestQcStartedEventId,
                        evidenceCount = policy.evidenceCount,
                        evidenceCounts = policy.evidenceCounts,
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        actionErrorMessage = throwable.message
                            ?: "Не удалось сохранить AR скриншот",
                    )
                }
        }
    }

    private fun performQcOutcome(block: suspend (String) -> QcDecisionResult) {
        val workItemId = _uiState.value.workItemId ?: return
        viewModelScope.launch {
            if (!_uiState.value.canCompleteQc) {
                _uiState.value = _uiState.value.copy(
                    actionErrorMessage = missingEvidenceMessage(_uiState.value.missingEvidence),
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                actionInProgress = true,
                actionErrorMessage = null,
            )

            runCatching { block(workItemId) }
                .onSuccess { result ->
                    when (result) {
                        is QcDecisionResult.MissingEvidence -> _uiState.value = _uiState.value.copy(
                            actionInProgress = false,
                            canCompleteQc = false,
                            missingEvidence = result.missing,
                            actionErrorMessage = missingEvidenceMessage(result.missing),
                        )

                        QcDecisionResult.Success -> {
                            refreshPolicy(workItemId)
                            _uiState.value = _uiState.value.copy(actionInProgress = false)
                        }
                    }
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        actionInProgress = false,
                        actionErrorMessage = throwable.message
                            ?: "Не удалось завершить QC",
                    )
                }
        }
    }

    private suspend fun loadPolicy(workItemId: String): PolicySnapshot {
        val events = eventRepository.getEventsForWorkItem(workItemId)
        val latestQcStartedEventId = events
            .filter { it.type == EventType.QC_STARTED }
            .maxByOrNull { it.timestamp }
            ?.id
        val evidenceCounts = evidenceRepository.countsByKindForWorkItem(workItemId)
        val policyState = qcEvidencePolicy.evaluate(workItemId)

        return PolicySnapshot(
            canComplete = policyState.satisfied,
            missingEvidence = policyState.missing,
            latestQcStartedEventId = latestQcStartedEventId,
            evidenceCount = evidenceCounts.values.sum(),
            evidenceCounts = evidenceCounts,
        )
    }

    private data class LoadedPayload(
        val workItem: WorkItem,
        val policy: PolicySnapshot,
    )

    private data class PolicySnapshot(
        val canComplete: Boolean,
        val missingEvidence: Set<EvidenceKind>,
        val latestQcStartedEventId: String? = null,
        val evidenceCount: Int = 0,
        val evidenceCounts: Map<EvidenceKind, Int> = emptyMap(),
    )

    private fun missingEvidenceMessage(missing: Set<EvidenceKind>): String {
        if (missing.isEmpty()) return ""
        val parts = missing.map {
            when (it) {
                EvidenceKind.PHOTO -> "Фото отсутствует"
                EvidenceKind.AR_SCREENSHOT -> "AR скриншот отсутствует"
                EvidenceKind.VIDEO -> "Видео отсутствует"
                EvidenceKind.MEASUREMENT -> "Нет замеров"
            }
        }
        return parts.joinToString(separator = "; ")
    }
}
data class QcStartUiState(
    val isLoading: Boolean = false,
    val actionInProgress: Boolean = false,
    val workItemId: String? = null,
    val code: String? = null,
    val zone: String? = null,
    val canCompleteQc: Boolean = false,
    val missingEvidence: Set<EvidenceKind> = REQUIRED_KINDS,
    val latestQcStartedEventId: String? = null,
    val evidenceCount: Int = 0,
    val evidenceCounts: Map<EvidenceKind, Int> = emptyMap(),
    val actionErrorMessage: String? = null,
    val errorMessage: String? = null,
)

private val REQUIRED_KINDS = setOf(EvidenceKind.PHOTO, EvidenceKind.AR_SCREENSHOT)
