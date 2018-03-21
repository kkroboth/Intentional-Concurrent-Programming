export function mapPoints() {
    return fetch('/api/map/points', {
        credentials: 'same-origin',
        mode: 'cors',
        redirect: 'follow',
        referrer: 'no-referrer'
    })
        .then(res => res.json())
}

export function retrievePoint(id) {
    return fetch(`/api/map/point/${id}`, {
        credentials: 'same-origin',
        mode: 'cors',
        redirect: 'follow',
        referrer: 'no-referrer'
    }).then(res => res.json())
}
