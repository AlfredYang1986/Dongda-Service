package bmlogic.common.files

import java.io.File

import play.api.libs.Files
import java.io.FileInputStream

import com.pharbers.ErrorCode
import play.api.mvc.MultipartFormData
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.Json
import play.api.libs.json.Json._
import play.api.libs.json.JsValue

object fop {
	def uploadFile(data : MultipartFormData[TemporaryFile]) : JsValue = {
	  	data.file("upload").map { x =>
            Files.TemporaryFile(x.ref.file).moveTo(new File("images/" + x.filename), true)

			Json.toJson(Map("status" -> toJson("ok"),
                            "file_name" -> toJson(x.filename),
                            "result" -> toJson("success")))
	  	  	
	  	}.getOrElse {
			ErrorCode.errorToJson("post image error")
	  	} 
	}

	def downloadFile(name : String) : Array[Byte] = {
	  	val file = new File("images/" + name)
		val reVal : Array[Byte] = new Array[Byte](file.length.intValue)
		new FileInputStream(file).read(reVal)
		reVal
	}
}