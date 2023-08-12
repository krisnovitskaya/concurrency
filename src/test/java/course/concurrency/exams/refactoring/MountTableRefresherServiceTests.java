package course.concurrency.exams.refactoring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;

public class MountTableRefresherServiceTests {

    private MountTableRefresherService service;
    private Others.RouterStore routerStore;
    private Others.MountTableManager manager;
    private Others.MountTableManager managerFalse;
    private Others.MountTableManager managerSlow;
    private Others.MountTableManager managerException;
    private Others.LoadingCache routerClientsCache;

    @BeforeEach
    public void setUpStreams() {
        service = new MountTableRefresherService();
        service.setCacheUpdateTimeout(1000);
        routerStore = mock(Others.RouterStore.class);
        manager = mock(Others.MountTableManager.class);
        managerFalse = mock(Others.MountTableManager.class);
        managerSlow = mock(Others.MountTableManager.class);
        managerException = mock(Others.MountTableManager.class);
        service.setRouterStore(routerStore);
        routerClientsCache = mock(Others.LoadingCache.class);
        service.setRouterClientsCache(routerClientsCache);
         service.serviceInit(); // needed for complex class testing, not for now
    }

    @AfterEach
    public void restoreStreams() {
         service.serviceStop();
    }

    @Test
    @DisplayName("All tasks are completed successfully")
    public void allDone() {
        // given
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        when(manager.refresh()).thenReturn(true);

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);

        // smth more
        for (Others.RouterState state : states) {
            String admAddress = state.getAdminAddress();
            if(admAddress.startsWith("local")){
                when(mockedService.getLocalRefresher(admAddress)).thenReturn(new MountTableRefresher(manager, admAddress));
            }else {
                when(mockedService.getRefresher(admAddress)).thenReturn(new MountTableRefresher(manager, admAddress));
            }
        }

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

        when(managerFalse.refresh()).thenReturn(false);

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);

        // smth more
        for (Others.RouterState state : states) {
            String admAddress = state.getAdminAddress();
            if(admAddress.startsWith("local")){
                when(mockedService.getLocalRefresher(admAddress)).thenReturn(new MountTableRefresher(manager, admAddress));
            }else {
                when(mockedService.getRefresher(admAddress)).thenReturn(new MountTableRefresher(manager, admAddress));
            }
        }
        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=0,failureCount=4");
    }

    @Test
    @DisplayName("Some tasks failed")
    public void halfSuccessedTasks() {
        // given
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123", "local6", "789", "local");

        when(manager.refresh()).thenReturn(true);
        when(managerFalse.refresh()).thenReturn(false);

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        // smth more

        for (var state : states) {
            String admAddress = state.getAdminAddress();
            if(admAddress.startsWith("local")){
                when(mockedService.getLocalRefresher(admAddress)).thenReturn(new MountTableRefresher(managerFalse, admAddress));
            }else {
                when(mockedService.getRefresher(admAddress)).thenReturn(new MountTableRefresher(manager, admAddress));
            }
        }


        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("Mount table entries cache refresh successCount=2,failureCount=2");
    }

    @Test
    @DisplayName("One task completed with exception")
    public void exceptionInOneTask() {
        // given
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123");

        when(managerException.refresh()).thenThrow(new RuntimeException("test exception"));

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);
        // smth more
        String admAddress = states.get(0).getAdminAddress();
        when(mockedService.getRefresher(admAddress)).thenReturn(new MountTableRefresher(managerException, admAddress));

        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("refresher with address="+ admAddress + "; throw ex: message=java.lang.RuntimeException: test exception");
        verify(mockedService).log("Mount table entries cache refresh successCount=0,failureCount=1");
    }

    @Test
    @DisplayName("One task exceeds timeout")
    public void oneTaskExceedTimeout() {
        // given
        MountTableRefresherService mockedService = Mockito.spy(service);
        List<String> addresses = List.of("123");

        when(managerSlow.refresh()).then((res) -> {
            Thread.sleep(3000);
            return true;
        });

        List<Others.RouterState> states = addresses.stream()
                .map(a -> new Others.RouterState(a)).collect(toList());
        when(routerStore.getCachedRecords()).thenReturn(states);

        // smth more
        String admAddress = states.get(0).getAdminAddress();
        when(mockedService.getRefresher(admAddress)).thenReturn(new MountTableRefresher(managerSlow, admAddress));

        // when
        mockedService.refresh();

        // then
        verify(mockedService).log("refresher with address=" + admAddress + " exceed timeout");
        verify(mockedService).log("Mount table entries cache refresh successCount=0,failureCount=1");
    }

}
