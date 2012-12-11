package es.upm.fi.oeg.morph.db
import es.upm.fi.oeg.morph.relational.JDBCRelationalModel
import java.util.Properties
import java.sql.DriverManager
import com.hp.hpl.jena.datatypes.RDFDatatype
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype
import org.postgresql.util.PSQLException

class Rdb(props:Properties) extends JDBCRelationalModel(props){
  private def getConnection=
      DriverManager.getConnection(sourceUrl,user,password)
  
  private def prepare(s:Object)={
    if (s==null) "NULL"
    else s match{
      case st:String=>"'"+s+"'"
      case _=>s.toString
    }
  }
    
  def insert(table:String,vals:Iterable[Array[Object]])={
    val con=getConnection
    val values=vals.map(v=>"("+v.map(s=>prepare(s)).mkString(",")+")").mkString(",")
    val statement="INSERT INTO "+table+" VALUES"+values //("+vals.map(s=>prepare(s)).mkString(",")+")"
    println(statement)
    try con.prepareStatement(statement).executeUpdate
    catch {case e:PSQLException=>
      if (e.getMessage.contains("duplicate")) println(e.getMessage)
      else throw e}
    con.close
  }

  def getAll(q:String,colNames:Array[String])={
    val con=getConnection
    val res=con.prepareStatement(q).executeQuery
    val str=Stream.continually({val hasnext=res.next;hasnext}).map{a=>
      colNames.map{c=>res.getObject(c)}
    }
      
    res.close
    con.close
    
    //result
  }

  
  def queryFirst(q:String,colNames:Array[String])={
    val con=getConnection
    val res=con.prepareStatement(q).executeQuery
    val hasnext=res.next
    val result=if (!hasnext) null 
      else colNames.map{c=>res.getObject(c)}
    res.close
    con.close
    
    result
  }

}