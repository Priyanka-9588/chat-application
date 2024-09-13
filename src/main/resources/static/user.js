let loggedInUsername = "";
let userPassword = "";

// Show the signup form
function showSignUpForm() {
    document.getElementById('signUpForm').style.display = 'block';
    document.getElementById('loginForm').style.display = 'none';
    document.getElementById('otpForm').style.display = 'none';
    document.getElementById('chatSection').style.display = 'none';
   // document.getElementById('messageHistory').style.display = 'none';
}

// Show the login form
function showLoginForm() {
    document.getElementById('signUpForm').style.display = 'none';
    document.getElementById('otpForm').style.display = 'none';
    document.getElementById('loginForm').style.display = 'block';
    document.getElementById('chatSection').style.display = 'none';
   // document.getElementById('messageHistory').style.display = 'none';
}
function showOtpForm(email) {
    document.getElementById('otpForm').style.display = 'block';
    document.getElementById('signUpForm').style.display = 'none';
    document.getElementById('loginForm').style.display = 'none';
    document.getElementById('chatSection').style.display = 'none';
    document.getElementById('otpEmail').value = email;
}

// Show OTP form for verification
function showOtpFormForVerification() {
  const email = prompt('Please enter your email:'); // Get the email from the user
      if (email) {
          document.getElementById('otpEmail').value = email; // Pre-fill the email field
          showOtpForm(email);  // Show OTP form
      }
}

// Show the chat section
function showChatSection(username) {
    document.getElementById('loggedInUser').innerText = username;
    document.getElementById('chatSection').style.display = 'block';
    document.getElementById('loginForm').style.display = 'none';
    document.getElementById('otpForm').style.display = 'none';
    document.getElementById('signUpForm').style.display = 'none';
   // document.getElementById('messageHistory').style.display = 'block';

    // Populate the user dropdown and fetch messages
    populateToUserDropdown();
    fetchGroupedMessages();
}

// Sign up user
function signUpUser() {
    const username = document.getElementById('signUpUsername').value;
    const password = document.getElementById('signUpPassword').value;
    const name = document.getElementById('signUpName').value;
    const email = document.getElementById('signUpEmail').value;

  //  if (!username || !password || !name || !email) {
    //    alert('All fields are required.');
      //  return;
    //}
     if (!validateEmail()) {
            return;  // Stop if client-side validation fails
        }

    const data = { username, password, name, email };

    // Step 1: Create the user in the local database and send OTP for verification
    fetch('http://localhost:8080/api/signup', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        if (data === "User created successfully. Please verify your email.") {
            showOtpForm(email);  // Show OTP form for verification
        }
    })
    .catch(error => alert('Error: ' + error.message));
}

// Verify OTP and create user in Openfire
function verifyOtp() {
    const email = document.getElementById('otpEmail').value;
    const otp = document.getElementById('otpCode').value;

    if (!email || !otp) {
        alert('Both email and OTP are required.');
        return;
    }

    fetch(`http://localhost:8080/api/verify?email=${encodeURIComponent(email)}&otp=${encodeURIComponent(otp)}`, {
        method: 'POST',
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        if (data === "Email verified successfully!") {
            // Step 2: Create the user in Openfire after OTP verification
            createOpenfireUser();
            showLoginForm();
        }
    })
    .catch(error => alert('Error: ' + error.message));
}

// Create user in Openfire
function createOpenfireUser() {
    const username = document.getElementById('signUpUsername').value;
    const password = document.getElementById('signUpPassword').value;
    const name = document.getElementById('signUpName').value;
    const email = document.getElementById('signUpEmail').value;

    const data = { username, password, name, email };

    fetch('http://localhost:8080/api/createUserInOpenfire', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        if (data === "User created in Openfire successfully.") {
            showLoginForm();  // Redirect to login form after successful Openfire creation
        } else {
            alert('Failed to create user in Openfire: ' + data);
        }
    })
    .catch(error => alert('Error creating user in Openfire: ' + error.message));
}

// Login user
function loginUser() {
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;

    if (!username || !password) {
        alert('Both fields are required.');
        return;
    }

    fetch('http://localhost:8080/api/authenticateUser', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ username, password })
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        if (data === "User authenticated successfully.") {
            loggedInUsername = username;
            userPassword = password;
            showChatSection(username); // Show chat section after successful login
            //  populateToUserDropdown();  // Populate the user dropdown
              //           fetchGroupedMessages();

        }
    })
    .catch(error => alert('Error: ' + error.message));
}

// Populate the recipient dropdown with available users
function populateToUserDropdown() {
    fetch('/api/getAllUsers')
    .then(response => response.json())
    .then(users => {
        const toDropdown = document.getElementById('toUsername');
        toDropdown.innerHTML = "";

        users.forEach(user => {
            if (user.username !== loggedInUsername) {
                const option = document.createElement('option');
                option.value = user.username;
                option.textContent = user.username;
                toDropdown.appendChild(option);
            }
        });
    })
    .catch(error => alert("Error fetching users: " + error.message));
}

// Send chat message
function sendMessage() {
    const toUsername = document.getElementById('toUsername').value;
    const messageBody = document.getElementById('messageBody').value;

    if (!loggedInUsername || !userPassword || !toUsername || !messageBody) {
        alert('All fields (including sender and recipient usernames) must be filled out.');
        return;
    }

    const data = {
        username: loggedInUsername,
        password: userPassword,
        toUsername: toUsername,
        messageBody: messageBody
    };

    fetch('/api/sendMessage', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        fetchGroupedMessages();  // Refresh message history after sending
    })
    .catch(error => alert("Error: " + error.message));
}

// Fetch grouped messages (message history)
function fetchGroupedMessages() {
    const url = `/api/messages/history?loggedInUser=${encodeURIComponent(loggedInUsername)}`;

    fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(groupedMessages => {
        const groupedMessageHistory = document.getElementById('groupedMessageHistory');
        groupedMessageHistory.innerHTML = "";  // Clear previous history

        // Iterate through each user in the grouped messages
        Object.keys(groupedMessages).forEach(user => {
            const userSection = document.createElement('div');
            userSection.className = 'mb-4';

            // Display header with the username (without domain)
            const userHeader = document.createElement('h5');
            userHeader.textContent = `Conversation with ${extractUsername(user)}`;
            userSection.appendChild(userHeader);

            const messageList = document.createElement('ul');
            messageList.className = 'list-group';

            // Iterate through each message for the user
            groupedMessages[user].forEach(message => {
                const li = document.createElement('li');
                li.className = 'list-group-item';

                const formattedTimestamp = formatTimestamp(message.timestamp);
                const fromUser = extractUsername(message.fromUser); // Sender username without domain
                const toUser = extractUsername(message.toUser);     // Recipient username without domain

                // Format the message to be more readable
                li.textContent = `${formattedTimestamp}: ${fromUser} -> ${toUser}: ${message.body}`;
                messageList.appendChild(li);
            });

            userSection.appendChild(messageList);
            groupedMessageHistory.appendChild(userSection);
        });
    })
    .catch(error => alert("Error fetching messages: " + error.message));
}

// Function to validate the email and show a custom message
function validateEmail() {
    const emailField = document.getElementById('signUpEmail');
    const emailError = document.getElementById('emailError');

    // Check if email is valid using the browser's built-in check
    if (!emailField.checkValidity()) {
        emailField.classList.add('invalid-input');
        emailError.style.display = 'block';  // Show error message
        return false;  // Prevent form submission
    } else {
        emailField.classList.remove('invalid-input');
        emailError.style.display = 'none';  // Hide error message
        return true;  // Allow form submission
    }
}

// Attach event listener to the form submit action for the Sign Up form
document.addEventListener('DOMContentLoaded', function() {
    const signUpForm = document.getElementById('signUpForm');
    signUpForm.addEventListener('submit', function(event) {
        if (!validateEmail()) {
            event.preventDefault();  // Prevent form submission if email is invalid
        }
    });
});


// Helper function to format timestamp
function formatTimestamp(timestamp) {
    const date = new Date(timestamp);
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    let hours = date.getHours();
       const minutes = date.getMinutes().toString().padStart(2, '0');
       const ampm = hours >= 12 ? 'PM' : 'AM';  // Determine AM or PM

       hours = hours % 12;  // Convert to 12-hour format
       hours = hours ? hours : 12;  // If hours is 0, make it 12

       return `${day}/${month}/${year} ${hours}:${minutes} ${ampm}`
}
function extractUsername(fullUsername) {
    return fullUsername.split('@')[0]; // Extract the part before @
}
