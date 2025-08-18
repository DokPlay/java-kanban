import manager.InMemoryTaskManager;
import manager.TaskManagerTest;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

  @Override
  protected InMemoryTaskManager createManager() {
    return new InMemoryTaskManager();
  }
}
