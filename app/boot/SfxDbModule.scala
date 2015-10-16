package boot

import com.google.inject.AbstractModule

class SfxDbModule extends AbstractModule {
  protected def configure: Unit = {
    bind(classOf[DummyData]).asEagerSingleton()
  }
}
