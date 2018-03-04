export function mapPoints() {
    return fetch('/api/map/points', {
        credentials: 'same-origin',
        mode: 'cors',
        redirect: 'follow',
        referrer: 'no-referrer'
    })
        .then(res => res.text())
        .then(text => {
            return text.split("\n")
                .map(item => item.split(","))
                .map(loc => [parseInt(loc[0]), loc[1], loc[2], parseFloat(loc[3]), parseFloat(loc[4])])
        })
}
