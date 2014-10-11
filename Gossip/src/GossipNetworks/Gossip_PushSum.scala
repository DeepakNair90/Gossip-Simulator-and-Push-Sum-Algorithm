package GossipNetworks
import akka.actor._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.concurrent.duration._
import scala.math._
import scala.Mutable
import java.util.HashSet

case class Set(neighboursReceived: ArrayBuffer[ActorRef]) //extends GossipMessage
case object Start
case class Go()
case class PushSum(s: Double, w: Double)
case object Rumor
case object Finish
case class Finish_PushSum(roundCount: Int)
case object Wakeup
case object PushSum_Wakeup

case class Remove(actor:ActorRef)
case class RanAdd(ranN: ActorRef)
case class Set_s(svalue: Double)

object Gossip_PushSum extends App {
  
 override def main(args: Array[String])
{
   
  //var numNodesin2D: Int = 0
  var root=0
  
  if (args.length != 3) 
      println("Incorrect Arguments..!!")
   var numNodes:Int = args(0).toInt
   val topology:String = args(1)
   val algorithm:String = args(2)
   val system = ActorSystem("Network")
   
   if (topology == "2d" || topology == "imp2d")
  {
  root = ceil(sqrt(numNodes)).toInt
  numNodes = pow(root, 2).toInt
  //println("Root before : " + root)
  }

   
   val manager = system.actorOf(Props(new Manager(numNodes,topology,algorithm)),name = "manager")
   //Start
   
   manager ! Go()
   
   //println("Manager ! Go Done")
   
}
}

class Manager(numNodes: Int, topology: String, algorithm: String) extends Actor 
{
  
  val root=sqrt(numNodes).toInt
  //println("Root After : " + root)
  
  if (numNodes == 0) {
    context.system.shutdown()
}

  var nodes = new ArrayBuffer[ActorRef]()
  var temp = new ArrayBuffer[ActorRef]()
  var finishedNodes: Int = 0
  var time: Long = 0
  var randomnode = 0
  var nodesLeft=numNodes
  
  
    for (i <- 0 until numNodes) 
    {
    nodes += context.actorOf(Props(new GossipActor(numNodes)), name = "nodes" + i) //create all nodes (actors)
    }
    
  
    if (algorithm == "pushsum") {
    for (i <- 0 until numNodes) {
      nodes(i) ! Set_s(i.toDouble) //Set s
    }
  }
  
  
  //println("All nodes created")
  
    topology match  //Setting neighbors
    { 
      
      //inside topology match
      
      
   case "2d" =>
      nodes(0) ! Set(temp += (nodes(1), nodes(root))) //set neighbor for first element in first line
      temp = new ArrayBuffer[ActorRef]()

      for (i <- 1 to root - 2) {
        nodes(i) ! Set(temp += (nodes(i - 1), nodes(i + 1), nodes(i + root))) //first line except first and last
        temp = new ArrayBuffer[ActorRef]()
      }

      nodes(root - 1) ! Set(temp += (nodes(root - 2), nodes(root - 1 + root))) //last one of first line
      temp = new ArrayBuffer[ActorRef]()

      for (i: Int <- root to numNodes - root - 1) { //Middle lines
        if (i % root == 0) {
          nodes(i) ! Set(temp += (nodes(i - root), nodes(i + root), nodes(i + 1)))
          temp = new ArrayBuffer[ActorRef]()
        } else if (i % root == root - 1) {
          nodes(i) ! Set(temp += (nodes(i - root), nodes(i + root), nodes(i - 1)))
          temp = new ArrayBuffer[ActorRef]()
        } else {
          nodes(i) ! Set(temp += (nodes(i - root), nodes(i + root), nodes(i - 1), nodes(i + 1)))
          temp = new ArrayBuffer[ActorRef]()
        }
      }

      nodes(numNodes - root) ! Set(temp += (nodes(numNodes - root - root), nodes(numNodes - root + 1))) //set neighbor for first element in last line
      temp = new ArrayBuffer[ActorRef]()

      for (i <- numNodes - root + 1 to numNodes - 2) {
        nodes(i) ! Set(temp += (nodes(i - 1), nodes(i + 1), nodes(i - root))) //last line except first and last element
        temp = new ArrayBuffer[ActorRef]()
      }

      nodes(numNodes - 1) ! Set(temp += (nodes(numNodes - 2), nodes(numNodes - 1 - root))) //last one of last line
      temp = new ArrayBuffer[ActorRef]()   
   

      
      
      
      //IMPERFECT 2D
      
  
      
     case "imp2d" =>
       var done=new HashSet[Integer]()
       randomnode=Random.nextInt(numNodes)
       while (randomnode==0 || randomnode==1 || randomnode==root )
        randomnode=Random.nextInt(numNodes)
       if(done.contains(randomnode))
          nodes(0) ! Set(temp += (nodes(1), nodes(root)))
       else
       {
      done.add(randomnode)
      nodes(randomnode) ! Set((temp)+= (nodes(0)))
     temp = new ArrayBuffer[ActorRef]()
      nodes(0) ! Set(temp += (nodes(1), nodes(root),nodes(randomnode))) //set neighbor for first element in first line
       }
      temp = new ArrayBuffer[ActorRef]()

      for (i <- 1 to root - 2) {
        
         randomnode=Random.nextInt(numNodes)
         while (randomnode==i || randomnode==i-1 || randomnode==i+1 || randomnode== i + root )
          randomnode=Random.nextInt(numNodes)
        if(done.contains(randomnode))
        nodes(i) ! Set(temp += (nodes(i - 1), nodes(i + 1), nodes(i + root))) //first line except first and last
        else
        {
          done.add(randomnode)
          nodes(randomnode) ! Set((temp)+= (nodes(i)))
     temp = new ArrayBuffer[ActorRef]()
        
        nodes(i) ! Set(temp += (nodes(i - 1), nodes(i + 1), nodes(i + root),nodes(randomnode)))
        }
        temp = new ArrayBuffer[ActorRef]()
      }
       
       randomnode=Random.nextInt(numNodes)
        while (randomnode==root-1 || randomnode==root - 2 || randomnode==root - 1 + root)
         randomnode=Random.nextInt(numNodes)
         
          if(done.contains(randomnode))
            nodes(root - 1) ! Set(temp += (nodes(root - 2), nodes(root - 1 + root)))
            else
            {done.add(randomnode)
              nodes(randomnode) ! Set((temp)+= (nodes(root - 1)))
     temp = new ArrayBuffer[ActorRef]()
      nodes(root - 1) ! Set(temp += (nodes(root - 2), nodes(root - 1 + root),nodes(randomnode))) //last one of first line
            }
      temp = new ArrayBuffer[ActorRef]()

      for (i: Int <- root to numNodes - root - 1) { //Middle lines
        
        
        randomnode=Random.nextInt(numNodes)
        while (randomnode==i || randomnode==i-root || randomnode==i+root || randomnode== i + 1 )
         randomnode=Random.nextInt(numNodes)
        
         if(done.contains(randomnode))
         {
        if (i % root == 0) {
          nodes(i) ! Set(temp += (nodes(i - root), nodes(i + root), nodes(i + 1)))
          temp = new ArrayBuffer[ActorRef]()
        } else if (i % root == root - 1) {
          nodes(i) ! Set(temp += (nodes(i - root), nodes(i + root), nodes(i - 1)))
          temp = new ArrayBuffer[ActorRef]()
        } else {
          nodes(i) ! Set(temp += (nodes(i - root), nodes(i + root), nodes(i - 1), nodes(i + 1)))
          temp = new ArrayBuffer[ActorRef]()
        }
         }
          else
            {
            done.add(randomnode)
            nodes(randomnode) ! Set((temp)+= (nodes(i)))
     temp = new ArrayBuffer[ActorRef]()
             if (i % root == 0) {
               
          nodes(i) ! Set(temp += (nodes(i - root), nodes(i + root), nodes(i + 1),nodes(randomnode)))
          temp = new ArrayBuffer[ActorRef]()
        } else if (i % root == root - 1) {
          nodes(i) ! Set(temp += (nodes(i - root), nodes(i + root), nodes(i - 1),nodes(randomnode)))
          temp = new ArrayBuffer[ActorRef]()
        } else {
          nodes(i) ! Set(temp += (nodes(i - root), nodes(i + root), nodes(i - 1), nodes(i + 1),nodes(randomnode)))
          temp = new ArrayBuffer[ActorRef]()
        }
            }
      }

       randomnode=Random.nextInt(numNodes)
        while (randomnode== numNodes - root || randomnode==numNodes - root - root || randomnode==numNodes - root + 1)
         randomnode=Random.nextInt(numNodes)
       if(done.contains(randomnode))
         {nodes(numNodes - root) ! Set(temp += (nodes(numNodes - root - root), nodes(numNodes - root + 1)))}
       else{
       done.add(randomnode)
       nodes(randomnode) ! Set((temp)+= (nodes(numNodes - root)))
     temp = new ArrayBuffer[ActorRef]()
      nodes(numNodes - root) ! Set(temp += (nodes(numNodes - root - root), nodes(numNodes - root + 1),nodes(randomnode))) //set neighbor for first element in last line
       }
      temp = new ArrayBuffer[ActorRef]()
      
      
      

      for (i <- numNodes - root + 1 to numNodes - 2) {
        
        randomnode=Random.nextInt(numNodes)
        while (randomnode== i || randomnode==i-1 || randomnode==i+1 || randomnode== i - root )
         randomnode=Random.nextInt(numNodes)

         if(done.contains(randomnode))
         {nodes(i) ! Set(temp += (nodes(i - 1), nodes(i + 1), nodes(i - root)))}
       else{
       done.add(randomnode)
     nodes(randomnode) ! Set((temp)+= (nodes(i)))
     temp = new ArrayBuffer[ActorRef]()
        nodes(i) ! Set(temp += (nodes(i - 1), nodes(i + 1), nodes(i - root),nodes(randomnode))) //last line except first and last element
       }
        temp = new ArrayBuffer[ActorRef]()
      }
       
       randomnode=Random.nextInt(numNodes)
        while (randomnode== numNodes - 1 || randomnode==numNodes - 2 || randomnode==numNodes - 1 - root)
         randomnode=Random.nextInt(numNodes)
       
       
if(done.contains(randomnode))
         {
  nodes(numNodes - 1) ! Set(temp += (nodes(numNodes - 2), nodes(numNodes - 1 - root)))
         }
       else{
       done.add(randomnode)
     nodes(randomnode) ! Set((temp)+= (nodes(numNodes - 1)))
     temp = new ArrayBuffer[ActorRef]()
       nodes(numNodes - 1) ! Set(temp += (nodes(numNodes - 2), nodes(numNodes - 1 - root),nodes(randomnode))) //last one of last line
       }
      temp = new ArrayBuffer[ActorRef]()   
   
    
      
      

    case "full" =>
      for (i <- 0 until numNodes) 
      {
        nodes(i) ! Set(nodes - nodes(i)) //Set Neighbor Info
      }
    //println("topology full created")  
    
    case "line" =>

      nodes(0) ! Set(temp += nodes(1)) //set neighbor for first element in line
      temp = new ArrayBuffer[ActorRef]()

      for (i <- 1 to numNodes - 2) {
        nodes(i) ! Set(temp += (nodes(i - 1), nodes(i + 1))) //line except first and last
        temp = new ArrayBuffer[ActorRef]()
      }
      nodes(numNodes - 1) ! Set(temp += nodes(numNodes - 2)) //last one of line
      temp = new ArrayBuffer[ActorRef]()

     // println("Line Nodes Created")
      
      
    }

    
  def receive = {
    case Go() =>
      //println("Inside case Go")
     
      time = System.currentTimeMillis()
      if (algorithm == "gossip") 
      {
        //println("Inside Algo=Gossip")
        randomnode=Random.nextInt(numNodes)
        nodes(randomnode) ! Rumor
        //println("Sending Rumor to Node-"+randomnode)
        
      } 
      
      else 
      if (algorithm == "pushsum") 
      {
         randomnode=Random.nextInt(numNodes)
        nodes(randomnode) ! PushSum(0.0,0.0)
      } 
      else 
      {
        println("Incorrect Algorithm")
        context.system.shutdown()
      }   
      
     case Finish =>
       //println("In finish gossip")
       //println("NodesLeft : " + nodesLeft)
         nodesLeft-=1
        //println("NodesLeft : " + nodesLeft)
       
       if(nodesLeft==0)
       {
        println("Number of Nodes: " + numNodes)																										
        println("Time: " + (System.currentTimeMillis() - time) + " milliseconds")
        context.system.shutdown()
       }
       
       
       case Finish_PushSum =>
         /*println("In FinishPushSum with rounds : "+roundCount)
         println("NodesLeft : " + nodesLeft)
         nodesLeft-=1
         println("NodesLeft : " + nodesLeft)
       if(nodesLeft==0)
       {
        println("Number of Nodes: " + numNodes)	
        println("Converges after:"+roundCount)
        println("Time: " + (System.currentTimeMillis() - time) + " milliseconds")
        context.system.shutdown()*/
       
    
         println("Number of Nodes: " + numNodes)	
         println("Time: " + (System.currentTimeMillis() - time) + " milliseconds")
         context.system.shutdown()

       
  } 
}


//GossipActor

class GossipActor(numNodes: Int) extends Actor {
  import context._
  var neighbor = new ArrayBuffer[ActorRef]()
  var rumorCount: Int = 0
  var manager: ActorRef = null
  var isDone: Boolean = false
  var own_s: Double = 0.0
  var own_w: Double = 1.0
  var lastratio: Double = 0
  var ratio: Double = 0
  var roundCount: Int = 0
  var finished: Boolean = false
  var reached: Boolean = false
  var deadnodes: Int=0
  var randomNeigh: Int=0
  var nodesLeft=numNodes
  var stable_count=0

  
    def receive = {
    
    case Set(neighboursReceived) =>
     neighbor ++= neighboursReceived
   //  println("Self is : "+self + "neighbor is :"+ neighbor)
     manager = sender
     
     
       case PushSum(s, w) =>
        // println("s:"+s+"  w:" +w)
        own_s = (s + own_s) / 2
        own_w = (w + own_w) / 2
        ratio = own_s / own_w
        self ! PushSum_Wakeup

          /*changed!!
          valueCount += 1
        } else {
          valueCount = 0
        }
        lastratio = ratio
        if (valueCount < 30 && neighbor.length > 0) {
          //context.system.scheduler.scheduleOnce(0.1 milliseconds, neighbor(Random.nextInt(neighbor.length)), Push(mys, myw))
          neighbor(Random.nextInt(neighbor.length)) ! Push(mys, myw)
          //          if (!reached) {
          //            self ! PushRemind
          //            reached = true
          //          }
        } else {
          isDone = true
          for (ac: ActorRef <- neighbor)
            ac ! RemoveMe
          if (!finished) {
            boss ! Finish
            finished = true
          }

          //context.stop(self)
        }

      */
    case PushSum_Wakeup =>
      if (Math.abs(ratio - lastratio) < 0.0000000001)
      {
      stable_count+=1 
      //println("Stablecount for:"+ self+" is "+ stable_count)
      }
      else
      stable_count=0
      
      lastratio=ratio
      
      if(stable_count<3 && neighbor.length > 0 )
      {
      val pqr=Random.nextInt(neighbor.length)
      context.system.scheduler.scheduleOnce(0 milliseconds, neighbor(pqr), PushSum(own_s, own_w))
      println(own_s/own_w)
      //println("Called---->" + pqr)
      }
      
      else
      {   
        /*println("Else Part ----> " + self)
        for (i <- 0 to neighbor.length-1)
        {
          neighbor(i)!Remove(self)
        }
       // println("Dying:"+self)
        manager ! Finish_PushSum
        context.stop(self)*/
        
        manager ! Finish_PushSum
        
      }
      
 
    case Rumor =>
         rumorCount+=1
        //println("Inside Rumor")
        self ! Wakeup

  
     case Wakeup =>
       //println("Inside Wakeup")
        if (rumorCount < 10  && neighbor.length > 0) {
        randomNeigh=Random.nextInt(neighbor.length)
        neighbor(randomNeigh) ! Rumor
       // println(context.self + " is sending msg to " + neighbor(randomNeigh))
       //println("sent rumor again")
        context.system.scheduler.scheduleOnce(10 milliseconds, self, Wakeup)
        //println("") 
        }
        
      else
      {
        //println("Self = " + self)
        for (i <- 0 to neighbor.length-1)
        {
          neighbor(i)!Remove(self)
        }
       // println("Dying:"+self)
        manager ! Finish
        context.stop(self)

      /*  
        deadnodes+=1
        for (act: ActorRef <- neighbor)
        {
        context.stop(act)
        println(act + " is dead")
        //println("Dead here")
        deadnodes+=1
        println("Number of DeadNodes : "+deadnodes+ "NumNodes : "+numNodes)
        }
        if(deadnodes==numNodes) 
        { 
        manager ! Finish
        println("Called Manager ! Finish")
        }
        //println("Rumor count = :"+rumorCount)
      */  
     
      }     
    
      case Remove(toBedeleted : ActorRef) =>
       neighbor-=toBedeleted
       //println("Neighbor = " + neighbor)

      /* case RanAdd(ranN) =>
      neighbor += ranN */
       
      case Set_s(svalue: Double) =>
       own_s=svalue
       
       
      
       
       
      
       
       
  }

}















   
   
  
  
	  