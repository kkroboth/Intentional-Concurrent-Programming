export function mapPoints() {
    return fetch('/api/map/points', {
        credentials: 'same-origin',
        mode: 'cors',
        redirect: 'follow',
        referrer: 'no-referrer'
    })
        .then(res => res.json())
}
