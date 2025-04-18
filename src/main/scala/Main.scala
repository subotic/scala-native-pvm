import scala.scalanative.unsafe.*
import scala.scalanative.unsigned.*
import scala.scalanative.libc.stdlib.{free, malloc}
import scala.scalanative.libc.string.memset
import scala.scalanative.libc.errno.errno
import scala.scalanative.libc.*

val PROT_READ     = 0x1
val PROT_WRITE    = 0x2
val PROT_EXEC     = 0x4
val MAP_PRIVATE   = 0x02
val MAP_ANONYMOUS = 0x20
val MAP_FAILED    = -1L

@extern
object libc:
  def mmap(addr: Ptr[Byte], length: CSize, prot: CInt, flags: CInt, fd: CInt, offset: CSize): Ptr[Byte] = extern
  def mprotect(addr: Ptr[Byte], len: CSize, prot: CInt): CInt = extern
  def munmap(addr: Ptr[Byte], length: CSize): CInt = extern

object Main:
    def main(args: Array[String]): Unit =
      val pageSize = 4096
      val addr = libc.mmap(null, pageSize.toUInt, PROT_READ | PROT_WRITE, MAP_PRIVATE | MAP_ANONYMOUS, -1, 0L.toCSize)
      if (addr.toLong == MAP_FAILED)
        throw new RuntimeException("mmap failed")

      val code: Array[Byte] = Array(
        0xb8.toByte, 0x02, 0x00, 0x00, 0x00, // mov eax, 2
        0x83.toByte, 0xc0.toByte, 0x03, // add eax, 3
        0xc3.toByte // ret
      )

      for i <- code.indices do
        val ptr = (addr + i.toUInt).asInstanceOf[Ptr[Byte]]
        !ptr = code(i)

      if libc.mprotect(addr, pageSize.toUInt, PROT_READ | PROT_EXEC) != 0 then
        throw new RuntimeException("mprotect failed")

      val func = CFuncPtr.fromPtr[CFuncPtr0[Int]](addr)
      val result = func()
      println(s"Result: $result")

      libc.munmap(addr, pageSize.toUInt)
