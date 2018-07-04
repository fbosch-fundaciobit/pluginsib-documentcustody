package es.caib.signatura.utils;

import org.apache.log4j.*;

/**
 * Clase que encapsula un log utilizando la API Log4J. El comportamiento de esta
 * clase es el siguiente:
 * <p>
 *  Si el nivel es NONE no se registra log para ningun tipo<br>
 *  Si el nivel es DEBUG se registran todos los tipos de log (DEBUG..FATAL)<br>
 *  Si el nivel es INFO se registran los tipos de log (INFO..FATAL)<br>
 *  Si el nivel es WARN se registran los tipos de log (WARN..FATAL)<br>
 *  Si el nivel es ERROR se registan los tipos de log (ERROR..FATAL)<br>
 *  Si el nivel es FATAL se registan los tipos de log (ERROR..FATAL)<br>
 * </p>
 * <p>El log se puede realizar sobre:<br>
 * La consola System.out (OUT)<br>
 * La consola System.err (ERR)<br>
 * Un fichero en disco (FILE)<br>
 * </p>
 */

public class BitLog
{
  /* Tipos de salida de BitLog. 
  public static final int SYSTEM_OUT = 0;
  public static final int SYSTEM_ERR = 1;
  public static final int SYSTEM_FILE = 2;
 */
  /* Configuracion de BitLog. 
  private static String name; // Nombre del log. Ejemplo: es.caib.serest.log
  private static Level level; // Nivel a aplicar el log como entero
  private static String levelName; // Nombre del nivel a aplicar el log. Ejemplo: DEBUG, INFO, etc.
  private static String format; // Formato de salida
  private static int out; // Indica si la salida es System.out o System.err
 */
  /* Logger de BitLog. 
  private static Logger logger; // Logger de la aplicacion
*/

  /**
   * Constructor de la clase BitLog
   * @param name nombre que tendro el log
   * @param level nivel de acceso a log
   * @param format formato de salida
   * @param out lugar donde se escribe el log
   */
  public static void creaBitLog(String name, String level, String format, String out)
  {
  /*
	try
    {
      setName(name); //establecemos el nombre del log
      setLevel(level); //convertimos el nivel como un String a un entero
      setLevelName(level); //establecemos el nombre del nivel
      setFormat(format); //establecemos el formato de salida
      setOut(out); //establecemos a donde direccionamos la salida   
      init(); //inicializamos el log
    }
    catch (Throwable t)
    {
      System.out.println("BitLog: name log: " + name);
      System.out.println("BitLog: level log: " + level);
      System.out.println("BitLog: format log: " + format);
      System.out.println("BitLog: out log: " + out);
      t.printStackTrace();
    }
    */
  }


  /**
   * Inicializa el log. Para ello establece en el root el tipo de salida
   * (PatternLayout) y a donde se redirecciona (system.err o system.out).
   * <p>
   * Luego crea el log con el nombre asociado. Establece el nivel de log a
   * DEBUG. Realmente el nivel se controla desde esta clase.
   * </p>
   */
  @SuppressWarnings({ "unused" })
  private static void init()
  {
	 /*
    logger = Logger.getLogger(name);
    Layout layout = new PatternLayout(format);

    // Establecemos el appender y layout
    if (getOut() == BitLog.SYSTEM_OUT)
    {
      logger.addAppender(new ConsoleAppender(layout, ConsoleAppender.SYSTEM_OUT));
    }
    else if (getOut() == BitLog.SYSTEM_ERR)
    {
      logger.addAppender(new ConsoleAppender(layout, ConsoleAppender.SYSTEM_ERR));
    }
    else if (getOut() == BitLog.SYSTEM_FILE)
    {
      try
      {
        logger.addAppender(new FileAppender(layout, getName() + ".log", true));
      }
      catch (Exception e)
      {
        System.out.println("BitLog: Error al crear el fichero de log con el nombre: " + getName());
      }
    }
    // Establecemos el nivel de log
    logger.setLevel(getLevel());
    */
  }


  /**
   * Realiza un log de nivel DEBUG
   * @param msg    Mensaje que queremos logear.
   */
  public static void debug(Object msg)
  {
   /* try
    {
      if (logger != null)
        logger.log(name, Level.DEBUG, msg, null);
      else
        System.out.println(msg.toString());
    }
    catch (Throwable t)
    {
      System.out.println("BitLog: Error en DEBUG");
      t.printStackTrace();
    }
    */
	  
	 //Logs por log4j
	 StackTraceElement [] stack=Thread.currentThread().getStackTrace();
	 for(int i=0;i<stack.length;i++){
		 if(!stack[i].getClassName().contains("BitLog") && stack[i].getClassName().contains("es.caib.signatura")){
			 Logger.getLogger(stack[i].getClassName()).debug(msg);
			 return;
		 }
	 }
	 Logger.getLogger(BitLog.class).debug(msg);
  }

  /**
   * Realiza un log de nivel INFO
   * @param msg    Mensaje que queremos logear.
   */
  public static void info(Object msg)
  {
    /*
	  try
    {
      if (logger != null)
        logger.log(name, Level.INFO, msg, null);
      else
        System.out.println(msg.toString());
    }
    catch (Throwable t)
    {
      System.out.println("BitLog: Error en INFO");
      t.printStackTrace();
    }
    */
		 //Logs por log4j
		 StackTraceElement [] stack=Thread.currentThread().getStackTrace();
		 for(int i=0;i<stack.length;i++){
			 if(!stack[i].getClassName().contains("BitLog") && stack[i].getClassName().contains("es.caib.signatura")){
				 Logger.getLogger(stack[i].getClassName()).info(msg);
				 return;
			 }
		 }
		 Logger.getLogger(BitLog.class).info(msg);

  }

  /**
   * Realiza un log de nivel WARN
   * @param msg    Mensaje que queremos logear.
   */
  public static void warn(Object msg)
  {
	  /*
    try
    {
      if (logger != null)
        logger.log(name, Level.WARN, msg, null);
      else
        System.out.println(msg.toString());
    }
    catch (Throwable t)
    {
      System.out.println("BitLog: Error en WARN");
      t.printStackTrace();
    }
    */
		 //Logs por log4j
		 StackTraceElement [] stack=Thread.currentThread().getStackTrace();
		 for(int i=0;i<stack.length;i++){
			 if(!stack[i].getClassName().contains("BitLog") && stack[i].getClassName().contains("es.caib.signatura")){
				 Logger.getLogger(stack[i].getClassName()).warn(msg);
				 return;
			 }
		 }
		 Logger.getLogger(BitLog.class).warn(msg);

  }

  /**
   * Realiza un log de nivel ERROR
   * @param msg    Mensaje que queremos logear.
   */
  public static void error(Object msg)
  {
	  /*
    try
    {
      if (logger != null)
        logger.log(name, Level.ERROR, msg, null);
      else
        System.out.println(msg.toString());
    }
    catch (Throwable t)
    {
      System.out.println("BitLog: Error en ERROR");
      t.printStackTrace();
    }
    */
		 //Logs por log4j
		 StackTraceElement [] stack=Thread.currentThread().getStackTrace();
		 for(int i=0;i<stack.length;i++){
			 if(!stack[i].getClassName().contains("BitLog") && stack[i].getClassName().contains("es.caib.signatura")){
				 Logger.getLogger(stack[i].getClassName()).error(msg);
				 return;
			 }
		 }
		 Logger.getLogger(BitLog.class).error(msg);

  }

  /**
   * Realiza un log de nivel ERROR
   * @param msg    Mensaje que queremos logear.
   */
  public static void error(Object msg,Throwable e)
  {
	  /*
    try
    {
      if (logger != null)
        logger.log(name, Level.ERROR, msg, null);
      else
        System.out.println(msg.toString());
    }
    catch (Throwable t)
    {
      System.out.println("BitLog: Error en ERROR");
      t.printStackTrace();
    }
    */
		 //Logs por log4j
		 StackTraceElement [] stack=Thread.currentThread().getStackTrace();
		 for(int i=0;i<stack.length;i++){
			 if(!stack[i].getClassName().contains("BitLog") && stack[i].getClassName().contains("es.caib.signatura")){
				 Logger.getLogger(stack[i].getClassName()).error(msg,e);
				 return;
			 }
		 }
		 Logger.getLogger(BitLog.class).error(msg,e);

  }
  /**
   * Realiza un log de nivel FATAL
   * @param msg    Mensaje que queremos logear.
   */
  public static void fatal(Object msg)
  {
	  /*
    try
    {
      if (logger != null)
        logger.log(name, Level.FATAL, msg, null);
      else
        System.out.println(msg.toString());
    }
    catch (Throwable t)
    {
      System.out.println("BitLog: Error en FATAL");
      t.printStackTrace();
    }
    */
		 //Logs por log4j
		 StackTraceElement [] stack=Thread.currentThread().getStackTrace();
		 for(int i=0;i<stack.length;i++){
			 if(!stack[i].getClassName().contains("BitLog") && stack[i].getClassName().contains("es.caib.signatura")){
				 Logger.getLogger(stack[i].getClassName()).fatal(msg);
				 return;
			 }
		 }
		 Logger.getLogger(BitLog.class).fatal(msg);

  }

  /**
   * Realiza un log de nivel FATAL
   * @param msg    Mensaje que queremos logear.
   */
  public static void fatal(Object msg,Throwable e)
  {
	  /*
    try
    {
      if (logger != null)
        logger.log(name, Level.FATAL, msg, null);
      else
        System.out.println(msg.toString());
    }
    catch (Throwable t)
    {
      System.out.println("BitLog: Error en FATAL");
      t.printStackTrace();
    }
    */
		 //Logs por log4j
		 StackTraceElement [] stack=Thread.currentThread().getStackTrace();
		 for(int i=0;i<stack.length;i++){
			 if(!stack[i].getClassName().contains("BitLog") && stack[i].getClassName().contains("es.caib.signatura")){
				 Logger.getLogger(stack[i].getClassName()).fatal(msg,e);
				 return;
			 }
		 }
		 Logger.getLogger(BitLog.class).fatal(msg,e);

  }

  /**
   * Permite establecer el nivel de log a un entero
   * @param newVal nivel de log en formato String para convertirlo a entero
   */
  @SuppressWarnings("unused")
  private static void setLevel(String newVal)
  {
	  /*
	  if (newVal == null)
      setLevel(Level.OFF);
    else if (newVal.equals("DEBUG"))
      setLevel(Level.DEBUG);
    else if (newVal.equals("INFO"))
      setLevel(Level.INFO);
    else if (newVal.equals("WARN"))
      setLevel(Level.WARN);
    else if (newVal.equals("ERROR"))
      setLevel(Level.ERROR);
    else if (newVal.equals("FATAL"))
      setLevel(Level.FATAL);
    // Si no es ninguno entonces no hacemos log
    else
      setLevel(Level.OFF);
      */
  }


  /**
   * Permite establecer la salida como entero
   * @param newVal salida del log que se convierte de String a entero
   */
  public static void setOut(String newVal)
  {
	  /*
    if (newVal == null)
      setOut(BitLog.SYSTEM_OUT);
    else if (newVal.equals("ERR"))
      setOut(BitLog.SYSTEM_ERR);
    else if (newVal.equals("FILE"))
      setOut(BitLog.SYSTEM_FILE);
    // Por defecto System.out
    else
      setOut(BitLog.SYSTEM_OUT);
      */
  }


  /**
   * Establece el nivel de log
   * @param newVal nivel de log
  
  public static void setLevel(Level newVal)
  {
    level = newVal;
  }
 */

  /**
   * Retorna el nivel de log
   * @return retorna el nivel de log

  public static Level getLevel()
  {
    return level;
  }
   */

  /**
   * Establece el formato de salida de log
   * @param newVal formato de log

  public static void setFormat(String newVal)
  {
    format = newVal;
  }
   */

  /**
   * Retorna el formato de log
   * @return retorna el formato de log
   
  public static String getFormat()
  {
    return format;
  }
   */

  /**
   * Establece donde se escribiro el log
   * @param newVal salida donde se logea
  public static void setOut(int newVal)
  {
    out = newVal;
  }
   */


  /**
   * Retorna el tipo de salida (SYSTEM_OUT o SYSTEM_ERR)
   * @return retorna el tipo de salida
  public static int getOut()
  {
    return out;
  }
   */


  /**
   * Establece el nombre del log.
   * @param newVal nombre del log
  private static void setName(String newVal)
  {
    name = newVal;
  }
   */


  /**
   * Retorna el nombre del log
   * @return retorna el nombre del log
  public static String getName()
  {
    return name;
  }
   */


  /**
   * Establece el nombre del nivel de log
   * @param newVal nombre del nivel de log
  public static void setLevelName(String newVal)
  {
    levelName = newVal;
  }
   */


  /**
   * Retorna el nombre del nivel de log
   * @return retorna el nombre del nivel de log
  public static String getLevelName()
  {
    return levelName;
  }
   */
  
}
