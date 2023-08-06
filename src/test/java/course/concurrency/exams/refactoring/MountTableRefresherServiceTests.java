package course.concurrency.exams.refactoring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockSettings;
import org.mockito.Mockito;
import org.mockito.internal.creation.MockSettingsImpl;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;

public class MountTableRefresherServiceTests {

    private MountTableRefresherService service;

    private Others.RouterStore routerStore;
    private Others.MountTableManager manager;
    private Others.LoadingCache routerClientsCache;

    @BeforeEach
    public void setUpStreams() {
        service = new MountTableRefresherService();
        service.setCacheUpdateTimeout(1000);
        routerStore = mock(Others.RouterStore.class);
        manager = mock(Others.MountTableManager.class);
        service.setRouterStore(routerStore);
        routerClientsCache = mock(Others.LoadingCache.class);
        service.setRouterClientsCache(routerClientsCache);
        // service.serviceInit(); // needed for complex class testing, not for now
    }

    @AfterEach
    public void restoreStreams() {
        // service.serviceStop();
    }

    @Test
    @DisplayName("All tasks are completed successfully")
    public void allDone() {
        // given
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");


        when(manager.refresh()).thenReturn(true);// это не работает



        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        // smth more
        when(mockedService.getRefresher("1231")).thenReturn(new MountTableRefresherThread(manager, "1231"));
        when(mockedService.getLocalRefresher("local62")).thenReturn(new MountTableRefresherThread(manager, "local62"));
        when(mockedService.getRefresher("7893")).thenReturn(new MountTableRefresherThread(manager, "7894"));
        when(mockedService.getLocalRefresher("local4")).thenReturn(new MountTableRefresherThread(manager, "local4"));


        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=4,failureCount=0");
        verify(routerClientsCache, never()).invalidate(anyString());
    }

    @Test
    @DisplayName("All tasks failed")
    public void noSuccessfulTasks() {
        // given
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");


        when(manager.refresh()).thenReturn(false);// это не работает



        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        // smth more
        when(mockedService.getRefresher("1231")).thenReturn(new MountTableRefresherThread(manager, "1231"));
        when(mockedService.getLocalRefresher("local62")).thenReturn(new MountTableRefresherThread(manager, "local62"));
        when(mockedService.getRefresher("7893")).thenReturn(new MountTableRefresherThread(manager, "7894"));
        when(mockedService.getLocalRefresher("local4")).thenReturn(new MountTableRefresherThread(manager, "local4"));


        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=0,failureCount=4");
    }

    @Test
    @DisplayName("Some tasks failed")
    public void halfSuccessedTasks() {

    }

    @Test
    @DisplayName("One task completed with exception")
    public void exceptionInOneTask() {

    }

    @Test
    @DisplayName("One task exceeds timeout")
    public void oneTaskExceedTimeout() {

    }

}
