package bmlogic

object AcitionType {
  case object follow extends ActionTypeDefines(0)
  case object unfollow extends ActionTypeDefines(1)
  case object like extends ActionTypeDefines(2)
  case object push extends ActionTypeDefines(3)
  case object loginOtherDevice extends ActionTypeDefines(4)
  case object unlike extends ActionTypeDefines(5)
  case object unpush extends ActionTypeDefines(6)
  case object message extends ActionTypeDefines(7) // may not use
  
  /**
    * order notify
    * 已经废弃
    */
  case object orderPushed extends ActionTypeDefines(10)
  case object orderAccecpted extends ActionTypeDefines(11)
  case object orderRejected extends ActionTypeDefines(12)
  case object orderAccomplished extends ActionTypeDefines(13)

  /**
    * order notify v3
    */
  case object al_posted extends ActionTypeDefines(20)
  case object al_rejected extends ActionTypeDefines(21)
  case object al_accepted extends ActionTypeDefines(22)
  case object al_paid extends ActionTypeDefines(23)
  case object al_cancel extends ActionTypeDefines(24)
  case object al_done extends ActionTypeDefines(25)
}

sealed abstract class ActionTypeDefines(val index : Int)