export function login(username, password) {
    return fetch('/api/login', {
        body: JSON.stringify({username, password}),
        cache: 'no-cache',
        credentials: 'same-origin',
        headers: {
            'Content-Type': 'application/json,'
        },
        method: 'POST',
        mode: 'cors',
        redirect: 'follow',
        referrer: 'no-referrer'
    })
        .then(res => res.json())
}