package net.polyv.android.player.common.modules.media.viewmodel.usecase

/**
 * @author Hoshiiro
 */
internal class PLVMPMediaUseCases(
    val updateMediaStateUseCase: UpdateMediaStateUseCase,
    val updateBufferingSpeedUseCase: UpdateBufferingSpeedUseCase,
    val observeNetworkPoorUseCase: ObserveNetworkPoorUseCase
)