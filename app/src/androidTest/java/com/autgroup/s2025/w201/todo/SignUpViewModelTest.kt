package com.autgroup.s2025.w201.todo

import com.autgroup.s2025.w201.todo.classes.SignUpViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SignUpViewModelTest {
    private lateinit var vm: SignUpViewModel

    @Before
    fun setUp() {
        vm = SignUpViewModel()
    }

    @Test
    fun validate_emptyEmail_returnsError() {
        val error = vm.validate("", "password123", "password123")
        Assert.assertEquals("Email cannot be empty", error)
    }

    @Test
    fun validate_invalidEmail_returnsError() {
        val error = vm.validate("invalidEmail", "password123", "password123")
        Assert.assertEquals("Invalid email format", error)
    }

    @Test
    fun validate_emptyPassword_returnsError() {
        val error = vm.validate("user@test.com", "", "")
        Assert.assertEquals("Password cannot be empty", error)
    }

    @Test
    fun validate_shortPassword_returnsError() {
        val error = vm.validate("user@test.com", "123", "123")
        Assert.assertEquals("Password must be at least 6 characters", error)
    }

    @Test
    fun validate_passwordsDoNotMatch_returnsError() {
        val error = vm.validate("user@test.com", "password123", "password321")
        Assert.assertEquals("Passwords do not match", error)
    }

    @Test
    fun validate_validInputs_returnsNull() {
        val error = vm.validate("user@test.com", "password123", "password123")
        Assert.assertEquals(null, error)
    }
}