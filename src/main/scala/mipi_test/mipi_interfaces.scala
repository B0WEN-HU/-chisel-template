// MIPI Interfaces
package mipi_test

import Chisel._

class MipiRxControlInterface extends Bundle {
  val DPHY_RSTN  = Bool()
  val RSTN       = Bool()
  val VC_ENA     = UInt(4.W)
  val LANES      = UInt(2.W)
  val CLEAR      = Bool()
}

class MipiRxVideoInterface extends Bundle {
  val VSYNC      = UInt(4.W)
  val HSYNC      = UInt(4.W)
  val VALID      = Bool()
  val CNT        = UInt(4.W)
  val DATA       = UInt(64.W)
  val TYPE       = UInt(6.W)
  val VC         = UInt(2.W)
  val ERR        = UInt(18.W)
}

class MipiTxControlInterface extends Bundle {
  val DPHY_RSTN  = Bool()
  val RSTN       = Bool()
  val LANES      = UInt(2.W)
}

class MipiTxVideoInterface extends Bundle {
  val VSYNC      = Bool()
  val HSYNC      = Bool()
  val VALID      = Bool()
  val HRES       = UInt(16.W)
  val DATA       = UInt(64.W)
  val TYPE       = UInt(6.W)
  val FRAME_MODE = Bool()
  val VC         = UInt(2.W)
  val ULPS_CLK_ENTER = Bool()
  val ULPS_CLK_EXIT  = Bool()
  val ULPS_ENTER = UInt(4.W)
  val ULPS_EXIT  = UInt(4.W)
}

///////////////////////////

class MipiRxInterfaces extends Bundle {
  val DPHY_RSTN  = Bool()
  val RSTN       = Bool()
  val VC_ENA     = UInt(4.W)
  val LANES      = UInt(2.W)
  val CLEAR      = Bool()

  val VSYNC      = UInt(4.W)
  val HSYNC      = UInt(4.W)
  val VALID      = Bool()
  val CNT        = UInt(4.W)
  val DATA       = UInt(64.W)
  val TYPE       = UInt(6.W)
  val VC         = UInt(2.W)
  val ERR        = UInt(18.W)
}

class MipiTxInterfaces extends  Bundle {
  val DPHY_RSTN  = Bool()
  val RSTN       = Bool()
  val LANES      = UInt(2.W)

  val VSYNC      = Bool()
  val HSYNC      = Bool()
  val VALID      = Bool()
  val HRES       = UInt(16.W)
  val DATA       = UInt(64.W)
  val TYPE       = UInt(6.W)
  val FRAME_MODE = Bool()
  val VC         = UInt(2.W)
  val ULPS_CLK_ENTER = Bool()
  val ULPS_CLK_EXIT  = Bool()
  val ULPS_ENTER = UInt(4.W)
  val ULPS_EXIT  = UInt(4.W)
}