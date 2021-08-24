package com.amarchaud.amdraganddrop.di

import com.amarchaud.amdraganddrop.domain.repo.ITestRepo
import com.amarchaud.amdraganddrop.domain.repo.TestRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TestRepoModule {
    @Binds
    abstract fun bindTestRepos(repo: TestRepo): ITestRepo
}