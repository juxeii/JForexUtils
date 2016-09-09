package com.jforex.programming.position.test;

import com.jforex.programming.test.common.InstrumentUtilForTest;

//@RunWith(HierarchicalContextRunner.class)
public class PositionUtilTest extends InstrumentUtilForTest {

//    private PositionUtil positionUtil;

//    @Mock
//    private OrderUtilImpl orderUtilHandlerMock;
//    @Mock
//    private PositionFactory positionFactoryMock;
//    @Mock
//    private Position positionMock;
//    @Mock
//    private IEngineUtil iengineUtilMock;
//    @Mock
//    private Action0 completeHandlerMock;
//    @Mock
//    private Action1<Throwable> errorHandlerMock;
//    @Mock
//    private Callable<IOrder> callableMock;
//    private final String mergeOrderLabel = "mergeOrderLabel";
//    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
//
//    @Before
//    public void setUp() {
//        setUpMocks();
//
//        positionUtil = new OrderUtilImpl(orderUtilHandlerMock,
//                                         positionFactoryMock,
//                                         iengineUtilMock);
//    }
//
//    public void setUpMocks() {
//        when(positionFactoryMock.forInstrument(instrumentEURUSD))
//            .thenReturn(positionMock);
//
//        when(iengineUtilMock.engine())
//            .thenReturn(engineMock);
//    }
//
//    private Observable<OrderEvent> cretaeObservable(final OrderEventType type) {
//        return eventObservable(buyOrderEURUSD, type);
//    }
//
//    private void verifyOnCompleteIsCalled() {
//        verify(completeHandlerMock).call();
//        verifyZeroInteractions(errorHandlerMock);
//    }
//
//    private void verifyOnErrorIsCalled() {
//        verify(errorHandlerMock).call(jfException);
//        verifyZeroInteractions(completeHandlerMock);
//    }
//
//    private CloseCommand closeCommandFactory(final IOrder order) {
//        return positionUtil
//            .closeBuilder(order)
//            .build();
//    }
//
//    private MergeCommand mergeCommandFactory(final Set<IOrder> toMergeOrders) {
//        return positionUtil
//            .mergeBuilder(mergeOrderLabel, toMergeOrders)
//            .build();
//    }
//
//    public class MergePositionSetup {
//
//        private Completable mergePosition;
//
//        private void subscribeForObservable(final Set<IOrder> positionOrders) {
//            when(orderUtilHandlerMock.callObservable(isA(MergeCommand.class)))
//                .thenReturn(emptyObservable());
//            when(positionMock.filled()).thenReturn(positionOrders);
//
//            subscribe();
//        }
//
//        private void subscribe() {
//            mergePosition = positionUtil.mergePosition(instrumentEURUSD,
//                                                       OrderUtilImplTest.this::mergeCommandFactory);
//            mergePosition.subscribe(completeHandlerMock, errorHandlerMock);
//        }
//
//        @Before
//        public void setUp() {
//            orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
//            orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.FILLED);
//        }
//
//        @Test
//        public void onCompleteIsCalledWhenPositionHasNoOrders() {
//            subscribeForObservable(Sets.newHashSet());
//
//            verify(completeHandlerMock).call();
//            verifyZeroInteractions(errorHandlerMock);
//        }
//
//        @Test
//        public void onCompleteIsCalledMergeCompletes() {
//            subscribeForObservable(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
//
//            verify(completeHandlerMock).call();
//            verifyZeroInteractions(errorHandlerMock);
//        }
//
//        @Test
//        public void errorIsEmittedWhenMergeFails() {
//            when(orderUtilHandlerMock.callObservable(isA(MergeCommand.class)))
//                .thenReturn(jfExceptionObservable());
//            when(positionMock.filled()).thenReturn(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
//            subscribe();
//
//            verifyOnErrorIsCalled();
//        }
//
//        @Test
//        public void whenNotSubscribedTheCompletableIsDeferred() {
//            positionUtil.mergePosition(instrumentEURUSD,
//                                       OrderUtilImplTest.this::mergeCommandFactory);
//
//            verifyZeroInteractions(orderUtilHandlerMock);
//            verifyZeroInteractions(positionMock);
//        }
//    }
//
//    public class ClosePositionSetup {
//
//        private Completable closePosition;
//
//        private void subscribeForObservable(final Set<IOrder> positionOrders) {
//            when(orderUtilHandlerMock.callObservable(isA(MergeCommand.class)))
//                .thenReturn(emptyObservable());
//            when(orderUtilHandlerMock.callObservable(isA(CloseCommand.class)))
//                .thenReturn(emptyObservable());
//            when(positionMock.filled()).thenReturn(positionOrders);
//            when(positionMock.filledOrOpened()).thenReturn(positionOrders);
//
//            subscribe();
//        }
//
//        private void subscribe() {
//            closePosition = positionUtil.closePosition(instrumentEURUSD,
//                                                       OrderUtilImplTest.this::mergeCommandFactory,
//                                                       OrderUtilImplTest.this::closeCommandFactory);
//            closePosition.subscribe(completeHandlerMock, errorHandlerMock);
//        }
//
//        @Before
//        public void setUp() {
//            orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
//            orderUtilForTest.setState(buyOrderEURUSD2, IOrder.State.FILLED);
//            orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.OPENED);
//        }
//
//        @Test
//        public void onCompleteIsCalledWhenPositionHasNoOrders() {
//            subscribeForObservable(Sets.newHashSet());
//
//            verify(completeHandlerMock).call();
//            verifyZeroInteractions(errorHandlerMock);
//        }
//
//        @Test
//        public void ifPositionHasOneOrderNoMergeIsCalled() {
//            subscribeForObservable(Sets.newHashSet(buyOrderEURUSD));
//
//            verify(completeHandlerMock).call();
//            verifyZeroInteractions(errorHandlerMock);
//        }
//
//        @Test
//        public void whenNotSubscribedTheCompletableIsDeferred() {
//            closePosition = positionUtil.closePosition(instrumentEURUSD,
//                                                       OrderUtilImplTest.this::mergeCommandFactory,
//                                                       OrderUtilImplTest.this::closeCommandFactory);
//
//            verifyZeroInteractions(orderUtilHandlerMock);
//            verifyZeroInteractions(positionMock);
//        }
//
//        public class ClosePositionWithMergeRequired {
//
//            @Test
//            public void onCompleteIsCalledWhenAllCommandsComplete() throws JFException {
//                orderUtilForTest.setLabel(buyOrderEURUSD, mergeOrderLabel);
//                when(orderUtilHandlerMock.callObservable(isA(MergeCommand.class)))
//                    .then(i -> {
//                        final MergeCommand command = (MergeCommand) i.getArguments()[0];
//                        final Set<IOrder> toMergeOrders = command.toMergeOrders();
//                        toMergeOrders.forEach(order -> {
//                            orderUtilForTest.setState(order, IOrder.State.CLOSED);
//                        });
//                        when(positionMock.filledOrOpened()).thenReturn(Sets.newHashSet(sellOrderEURUSD));
//                        return null;
//                    });
//                final Set<IOrder> positionOrders = Sets.newHashSet(buyOrderEURUSD, buyOrderEURUSD2, sellOrderEURUSD);
//                when(orderUtilHandlerMock.callObservable(isA(CloseCommand.class)))
//                    .thenReturn(emptyObservable());
//                when(positionMock.filled()).thenReturn(positionOrders);
//                when(positionMock.filledOrOpened()).thenReturn(positionOrders);
//
//                subscribe();
//
//                verify(completeHandlerMock).call();
//                verifyZeroInteractions(errorHandlerMock);
//            }
//        }
//    }
//
//    public class CloseAllPositionsSetup {
//
//        private Completable closeAllPositions;
//
//        private void subscribe() {
//            closeAllPositions = positionUtil.closeAllPositions(OrderUtilImplTest.this::mergeCommandFactory,
//                                                               OrderUtilImplTest.this::closeCommandFactory);
//            closeAllPositions.subscribe(completeHandlerMock, errorHandlerMock);
//        }
//
//        @Before
//        public void setUp() {
//            orderUtilForTest.setState(buyOrderEURUSD, IOrder.State.FILLED);
//            orderUtilForTest.setState(sellOrderEURUSD, IOrder.State.OPENED);
//        }
//
//        @Test
//        public void onCompleteIsCalledWhenNoPositionCreated() {
//            when(positionFactoryMock.allPositions())
//                .thenReturn(Sets.newHashSet());
//
//            subscribe();
//
//            verify(completeHandlerMock).call();
//            verifyZeroInteractions(errorHandlerMock);
//        }
//
//        @Test
//        public void whenNotSubscribedTheCompletableIsDeferred() {
//            closeAllPositions = positionUtil.closeAllPositions(OrderUtilImplTest.this::mergeCommandFactory,
//                                                               OrderUtilImplTest.this::closeCommandFactory);
//
//            verifyZeroInteractions(orderUtilHandlerMock);
//            verifyZeroInteractions(positionMock);
//        }
//
//        @Test
//        public void onCompleteIsCalledWhenAllPositionsAreClosed() {
//            final Position positionMockOne = mock(Position.class);
//            final Position positionMockTwo = mock(Position.class);
//
//            when(positionFactoryMock.forInstrument(instrumentEURUSD))
//                .thenReturn(positionMockOne);
//            when(positionFactoryMock.forInstrument(instrumentAUDUSD))
//                .thenReturn(positionMockTwo);
//
//            when(positionMockOne.instrument())
//                .thenReturn(instrumentEURUSD);
//            when(positionMockTwo.instrument())
//                .thenReturn(instrumentAUDUSD);
//            when(positionMockOne.filledOrOpened())
//                .thenReturn(Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD));
//            when(positionMockTwo.filledOrOpened())
//                .thenReturn(Sets.newHashSet(sellOrderAUDUSD));
//            when(positionFactoryMock.allPositions())
//                .thenReturn(Sets.newHashSet(positionMockOne, positionMockTwo));
//
//            subscribe();
//
//            // verify(startActionMock).call();
//            // verify(completeHandlerMock).call();
//            // verifyZeroInteractions(errorHandlerMock);
//        }
//    }
}
