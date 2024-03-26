/*
 * Copyright (c) 2024 Nicolas PICON
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package fr.nicopico.n2rss.controller.home

import fr.nicopico.n2rss.config.N2RssProperties
import fr.nicopico.n2rss.controller.Url
import fr.nicopico.n2rss.service.NewsletterService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import jakarta.validation.constraints.NotEmpty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.servlet.view.RedirectView
import java.net.URL

@Validated
@Controller
class HomeController(
    private val newsletterService: NewsletterService,
    props: N2RssProperties,
) {
    private val properties = props.feeds

    @GetMapping("/")
    fun home(request: HttpServletRequest, model: Model): String {
        val newslettersInfo = newsletterService.getNewslettersInfo()
            .filter { it.publicationCount > 0 }
        val requestUrl: String = request.requestURL
            .let {
                if (properties.forceHttps) {
                    it.replace(Regex("http://"), "https://")
                } else {
                    it.toString()
                }
            }

        with(model) {
            addAttribute("newsletters", newslettersInfo)
            addAttribute("requestUrl", requestUrl)
        }
        return "index"
    }

    @PostMapping("/send-request")
    fun requestNewsletter(
        @NotEmpty @Url @RequestParam("newsletterUrl") newsletterUrl: String
    ): RedirectView {
        newsletterService.saveRequest(URL(newsletterUrl))
        return RedirectView("/")
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleExceptions(
        exception: ConstraintViolationException,
    ): ResponseEntity<Map<String, String?>> {
        val errors = exception.constraintViolations.associate {
            it.propertyPath.toString() to it.message
        }
        return ResponseEntity.badRequest().body(errors)
    }
}
