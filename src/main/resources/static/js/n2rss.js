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

let newsletterForm = document.getElementById('newsletterForm');
let newsletterUrl = document.getElementById('newsletterUrl');
let sendRequest = document.getElementById("sendRequest");
let message = document.getElementById('message');
let recaptcha = document.getElementById('recaptcha');

function submitForm() {
    sendRequest.disabled = false;
    newsletterUrl.disabled = false;

    message.style.display = "none";

    fetch("/send-request", {method: 'POST', body: new FormData(newsletterForm)})
        .then(response => {
            if (!response.ok) {
                throw new Error("HTTP error " + response.status);
            }
            message.textContent = "Successfully requested!";
            message.classList.value = "message message-success"
        })
        .catch(() => {
            message.textContent = "There was an error sending the request. Please try again.";
            message.classList.value = "message message-error";
        })
        .finally(() => {
            message.style.display = "block";
            recaptcha.style.display = "none";
        });
}

newsletterForm
    .addEventListener('submit', function (event) {
        event.preventDefault();
        if (recaptcha) {
            sendRequest.disabled = true;
            newsletterUrl.disabled = true;
            recaptcha.style.display = "flex";

            message.textContent = "Please handle the captcha challenge to send your request";
            message.style.display = "block";
            message.classList.value = "message message-info";
        } else {
            submitForm();
        }
    });
