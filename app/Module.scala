import com.google.inject.AbstractModule

class Module extends AbstractModule {

  override def configure() = {
    // Ask Guice to create an instance of ApplicationTimer when the
    // application starts.
//    bind(classOf[MyExecutionContextImpl]).asEagerSingleton()
  }

}