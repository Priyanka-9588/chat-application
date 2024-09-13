const apiBaseUrl = 'http://localhost:8080/api';

function adminLogin() {

    const username = document.getElementById('adminUsername').value.trim();

    const password = document.getElementById('adminPassword').value.trim();


    if (username === 'admin' && password === 'decipher@123') {
    alert('Login successful');
        // Hide login form and show navigation buttons
        document.getElementById('adminLogin').style.display = 'none';
        document.getElementById('adminActionsNav').style.display = 'block';

        getAllUsersForDropdowns(); // Preload users for the dropdowns
    } else {
        alert('Unauthorized access. Only the admin can log in.');
    }
}

function showSection(sectionId) {
    // Hide all sections
    const sections = ['createUserSection', 'updateUserSection', 'deleteUserSection', 'listUsersSection'];
    sections.forEach(id => {
        document.getElementById(id).style.display = 'none';
    });

    // Show the selected section (this will not affect the buttons)
    document.getElementById(sectionId).style.display = 'block';
}

function createUser() {
    const data = {
        username: document.getElementById('createUsername').value,
        password: document.getElementById('createPassword').value,
        name: document.getElementById('createName').value,
        email: document.getElementById('createEmail').value,
    };

    fetch(`${apiBaseUrl}/createUser`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        getAllUsersForDropdowns();
        document.getElementById('createUsername').value = '';
        document.getElementById('createPassword').value = '';
        document.getElementById('createName').value = '';
        document.getElementById('createEmail').value = '';
    })
    .catch(error => alert('Error: ' + error.message));
}

function updateUser() {
    const data = {
        username: document.getElementById('updateUsername').value,
        name: document.getElementById('updateName').value,
    };

    fetch(`${apiBaseUrl}/updateUser`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        document.getElementById('updateName').value = '';
        getAllUsersForDropdowns();
    })
    .catch(error => alert('Error: ' + error.message));
}

function deleteUser() {
    const data = {
        username: document.getElementById('deleteUsername').value
    };

    fetch(`${apiBaseUrl}/deleteUser`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        getAllUsersForDropdowns();
    })
    .catch(error => alert('Error: ' + error.message));
}

function getAllUsers(){
    fetch(`${apiBaseUrl}/getAllUsers`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(users => {
        const userList = document.getElementById('allUsersList');
        userList.innerHTML = '';
        users.forEach(user => {
            const li = document.createElement('li');
            li.textContent = `${user.username} (${user.name}) - ${user.email}`;
            li.className = 'list-group-item';
            userList.appendChild(li);
        });
    })
    .catch(error => alert('Error fetching users: ' + error.message));
}

function getAllUsersForDropdowns() {
    fetch(`${apiBaseUrl}/getAllUsers`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(users => {
        const updateDropdown = document.getElementById('updateUsername');
        const deleteDropdown = document.getElementById('deleteUsername');

        updateDropdown.innerHTML = '';
        deleteDropdown.innerHTML = '';

        users.forEach(user => {
            const option = document.createElement('option');
            option.value = user.username;
            option.textContent = `${user.username} (${user.name})`;

            updateDropdown.appendChild(option);
            deleteDropdown.appendChild(option.cloneNode(true));
        });
    })
    .catch(error => alert('Error fetching users: ' + error.message));
}
