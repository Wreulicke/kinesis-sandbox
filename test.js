'use strict'

const fs = require('fs')
const puppeteer = require('puppeteer')
const base64 = require('urlsafe-base64')

fs.readFile('build/reports/deps/summary.dot', (e, data) =>{
    if (e != null ) {
        console.log(e)
        return
    }
    const content = data.toString();
    puppeteer.launch().then(async browser =>{
        const page = await browser.newPage();
        await page.injectFile("node_modules/viz.js/viz.js")
        await page.evaluate((content) => {
            const image = Viz(content, {format: 'png-image-element'})
            document.body.appendChild(image)
        }, content);
        const element = await page.$("img")
        const src = await element.evaluate(() => {
            const child=document.body.firstChild
            return child.src;
        })
        const img = base64.decode(src.split(',')[1])
        await new Promise((resolve, reject) =>{
            fs.writeFile("tmp.png", img, function(e){
                if(e === null)resolve()
                else reject(e)
            })
        })
        console.log()
        browser.close()
    })
})
