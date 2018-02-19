<template>
    <v-app>
        <v-content>
            <v-container fluid full-height>
                <v-layout align-center justify-center>
                    <v-flex xs12 sm8 md4>
                        <v-card class="elevation-12">
                            <v-toolbar dark color="primary">
                                <v-toolbar-title>Login form</v-toolbar-title>
                            </v-toolbar>
                            <v-card-text>
                                <v-form>
                                    <v-text-field prepend-icon="person" name="login" label="Login"
                                                  type="text" v-model="username"></v-text-field>
                                    <v-text-field prepend-icon="lock" name="password" label="Password" id="password"
                                                  type="password"
                                                  v-model="password"></v-text-field>
                                </v-form>
                            </v-card-text>
                            <v-card-actions>
                                <v-spacer></v-spacer>
                                <v-btn color="primary" @click="loginAction">Login</v-btn>
                            </v-card-actions>
                        </v-card>
                    </v-flex>
                </v-layout>
            </v-container>
        </v-content>
    </v-app>
</template>

<script>
    import {login} from '../api'

    export default {
        data() {
            return {
                username: null,
                password: null
            }
        },

        methods: {
            loginAction() {
                login(this.username, this.password)
                    .then(res => {
                        console.log(res)
                    })
            }
        }
    }
</script>

<style scoped>

</style>